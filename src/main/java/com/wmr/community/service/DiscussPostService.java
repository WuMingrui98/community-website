package com.wmr.community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.wmr.community.dao.CommentMapper;
import com.wmr.community.dao.DiscussPostMapper;
import com.wmr.community.entity.Comment;
import com.wmr.community.entity.DiscussPost;
import com.wmr.community.util.SensitiveFilter;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostService {
    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);
    private DiscussPostMapper discussPostMapper;

    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    // Caffeine的核心接口：Cache、LoadingCache、AsyncLoadingCache

    // 帖子列表的缓存
    private LoadingCache<String, List<DiscussPost>> postListCache;

    // 帖子总数的缓存
    private LoadingCache<Integer, Integer> postRowsCache;

    // 需要在bean创建后对缓存进行初始化
    @PostConstruct
    public void init() {
        // 初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    // CacheLoader的load方法定义了如果从缓存中找不到数据的话，应该从何处寻找数据
                    @Override
                    public @Nullable List<DiscussPost> load(@NonNull String key) throws Exception {
                        if (StringUtils.isBlank(key)) {
                            throw new IllegalArgumentException("参数错误!");
                        }
                        String[] split = key.split(":");
                        // 缓存的key的格式为offset:limit（userId = 0, orderModel = 1)
                        if (split.length != 2) {
                            throw new IllegalArgumentException("参数错误!");
                        }
                        int offset = Integer.parseInt(split[0]);
                        int limit = Integer.parseInt(split[1]);

                        // 这里是直接查mysql数据库
                        // 有二级缓存可以：本地缓存 -> Redis -> mysql

                        logger.debug("load post list from DB.");
                        return discussPostMapper.selectDiscussPosts(0, offset, limit, 1);
                    }
                });

        // 初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public @Nullable Integer load(@NonNull Integer key) throws Exception {
                        logger.debug("load post list from DB.");
                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });
    }


    @Autowired
    public void setDiscussPostMapper(DiscussPostMapper discussPostMapper) {
        this.discussPostMapper = discussPostMapper;
    }

    @Autowired
    public void setSensitiveFilter(SensitiveFilter sensitiveFilter) {
        this.sensitiveFilter = sensitiveFilter;
    }



    /**
     * 根据分页要求按用户id查询数据库中的帖子（userId=0，则查询所有用户的发帖数）
     * @param userId 用户id
     * @param offset mysql的offset
     * @param limit mysql的limit
     * @param orderMode 排序的模式: 0表示按照时间，1表示按照分数
     * @return 返回根据分页要求按用户id查询数据库中的帖子
     */
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int orderMode) {
        // 先从缓存中查
        if (userId == 0 && orderMode == 1) {
            return postListCache.get(offset + ":" + limit);
        }
        logger.debug("load post list from DB.");
        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }

    /**
     * 查询userId对应的用户的发帖数（userId=0，则查询所有用户的发帖数）
     * @param userId 用户id
     * @return 返回userId对应的用户的发帖数
     */
    public int findDiscussPostRows(int userId) {
        // 先从缓存中查
        if (userId == 0) {
            Integer res = postRowsCache.get(userId);
            if (res != null) return res;
        }
        logger.debug("load post rows from DB.");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    /**
     * 将discussPost处理后，传递给持久层处理
     * @param discussPost 初步封装的discussPost
     * @return 返回持久层的处理结果
     */
    public int addDiscussPost(DiscussPost discussPost) {
        if (discussPost == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        // 转移HTML标签
        String title = HtmlUtils.htmlEscape(discussPost.getTitle());
        String content = HtmlUtils.htmlEscape(discussPost.getContent());
        // 过滤敏感词
        title = sensitiveFilter.filter(title);
        content = sensitiveFilter.filter(content);

        discussPost.setContent(content);
        discussPost.setTitle(title);
        return discussPostMapper.insertDiscussPost(discussPost);
    }

    /**
     * 通过帖子的id找到帖子
     * @param id 帖子id
     * @return 返回对应id的DiscussPost
     */
    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }


    /**
     * 更新帖子的类型
     * @param id 帖子id
     * @param type 帖子类型 '0-普通; 1-置顶;'
     * @return 返回持久层返回的更新数
     */
    public int updateType(int id, int type) {
        return discussPostMapper.updateType(id, type);
    }

    /**
     * 更新帖子的状态
     * @param id 帖子id
     * @param status 帖子状态 '0-正常; 1-精华; 2-拉黑;'
     * @return 返回持久层返回的更新数
     */
    public int updateStatus(int id, int status) {
        return discussPostMapper.updateStatus(id, status);
    }


    /**
     * 更新帖子的分数
     * @param id 帖子id
     * @param score 分数
     * @return 返回持久层返回的更新数
     */
    public int updateScore(int id, double score) {
        return discussPostMapper.updateScore(id, score);
    }

}

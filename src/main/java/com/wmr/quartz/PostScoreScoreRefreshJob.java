package com.wmr.quartz;

import com.wmr.community.entity.DiscussPost;
import com.wmr.community.service.DiscussPostService;
import com.wmr.community.service.ElasticsearchService;
import com.wmr.community.service.LikeService;
import com.wmr.community.util.CommunityConstant;
import com.wmr.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

@Component
public class PostScoreScoreRefreshJob implements Job, CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(PostScoreScoreRefreshJob.class);

    private RedisTemplate<String, Object>  redisTemplate;

    private DiscussPostService discussPostService;

    private LikeService likeService;

    private ElasticsearchService elasticsearchService;

    // 牛客纪元
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化牛客纪元失败!", e);
        }
    }

    @Autowired
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Autowired
    public void setDiscussPostService(DiscussPostService discussPostService) {
        this.discussPostService = discussPostService;
    }

    @Autowired
    public void setLikeService(LikeService likeService) {
        this.likeService = likeService;
    }

    @Autowired
    public void setElasticsearchService(ElasticsearchService elasticsearchService) {
        this.elasticsearchService = elasticsearchService;
    }

    // job需要执行的任务
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // 从redis中取出要更新的帖子的分数
        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        Set<Object> postIds = redisTemplate.opsForSet().members(postScoreKey);

        if (postIds == null || postIds.size() == 0) {
            logger.info("[任务取消] 没有需要刷新的帖子!");
            return;
        }

        logger.info("[任务开始] 正在刷新帖子分数: " + postIds.size());
        for (Object postId: postIds) {
            refresh((Integer) postId);
            // 将postScoreKey删除对应的元素删除
            redisTemplate.opsForSet().remove(postScoreKey, postId);
        }
        logger.info("[任务结束] 帖子分数刷新完毕!");

    }


    /**
     * 计算帖子的分数
     * @param post 帖子
     * @return 返回帖子的分数
     */
    private double calScore(DiscussPost post) {
        // 是否为精华
        boolean wonderful = post.getStatus() == 1;
        // 评论数量
        int commentCount = post.getCommentCount();
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());

        // 计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10L + likeCount * 2;

        // 分数 = 帖子权重 + 距离天数
        return Math.log10(Math.max(w, 1))
                + (post.getCreateTime().getTime() - epoch.getTime()) / (86400000);
    }


    /**
     * 刷新帖子的分数，需要完成以下小功能
     * 1. 刷新mysql数据库中帖子的分数
     * 2. 刷新elasticsearch中帖子的分数
     *
     * @param postId 帖子id
     */
    private void refresh(int postId) {
        DiscussPost post = discussPostService.findDiscussPostById(postId);
        if (post == null) {
            logger.error("帖子不存在: id = " + postId);
            return;
        }
        double score = calScore(post);
        // 刷新mysql数据库中帖子的分数
        discussPostService.updateScore(postId, score);
        post.setScore(score);

        // 刷新elasticsearch中帖子的分数
        elasticsearchService.saveDiscussPost(post);
    }
}

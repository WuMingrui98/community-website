package com.wmr.community.service;

import com.wmr.community.dao.CommentMapper;
import com.wmr.community.dao.DiscussPostMapper;
import com.wmr.community.entity.Comment;
import com.wmr.community.entity.DiscussPost;
import com.wmr.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {
    private DiscussPostMapper discussPostMapper;

    private SensitiveFilter sensitiveFilter;


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
        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }

    /**
     * 查询userId对应的用户的发帖数（userId=0，则查询所有用户的发帖数）
     * @param userId 用户id
     * @return 返回userId对应的用户的发帖数
     */
    public int findDiscussPostRows(int userId) {
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

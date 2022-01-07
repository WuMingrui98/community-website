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

    private CommentMapper commentMapper;

    @Autowired
    public void setDiscussPostMapper(DiscussPostMapper discussPostMapper) {
        this.discussPostMapper = discussPostMapper;
    }

    @Autowired
    public void setSensitiveFilter(SensitiveFilter sensitiveFilter) {
        this.sensitiveFilter = sensitiveFilter;
    }


    @Autowired
    public void setCommentMapper(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    /**
     * 根据分页要求按用户id查询数据库中的帖子（userId=0，则查询所有用户的发帖数）
     * @param userId 用户id
     * @param offset mysql的offset
     * @param limit mysql的limit
     * @return 返回根据分页要求按用户id查询数据库中的帖子
     */
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit) {
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
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
     * 根据分页要求按entityType和entityId查询数据库中的评论或回复
     * 如果是回复(entityType=2)，不需要分页
     *
     * @param entityType 1表示评论 2表示回复
     * @param entityId 如果entityType=1时，表示当前Comment对象所在的帖子的id; 如果entityType=2时，表示回复的评论的id
     * @param offset mysql的offset
     * @param limit mysql的limit
     * @return 返回评论(回复)列表
     */
    public List<Comment> findComments(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectComments(entityType, entityId, offset, limit);
    }

    /**
     * 根据entityType和entityId返回数据库中评论(回复)的数量
     *
     * @param entityType 1表示评论 2表示回复
     * @param entityId 如果entityType=1时，表示当前Comment对象所在的帖子的id; 如果entityType=2时，表示回复的评论的id
     * @return 返回数据库中评论(回复)的数量
     */
    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }
}

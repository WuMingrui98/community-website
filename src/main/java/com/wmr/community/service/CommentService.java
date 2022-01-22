package com.wmr.community.service;

import com.wmr.community.dao.CommentMapper;
import com.wmr.community.dao.DiscussPostMapper;
import com.wmr.community.entity.Comment;
import com.wmr.community.util.CommunityConstant;
import com.wmr.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements CommunityConstant {

    private CommentMapper commentMapper;

    private DiscussPostMapper discussPostMapper;

    private SensitiveFilter sensitiveFilter;

    @Autowired
    public void setCommentMapper(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
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

    /**
     * 完成添加评论（回复）的功能，实现以下小的功能模块
     * 1. 对评论（回复）内容进行HTML格式转换和敏感词过滤
     * 2. 将comment对象中的数据添加到comment表
     * 3. 如果是评论帖子的话，修改discuss_post表中对应的comment_count项
     * 因为两个功能都要操作数据库，所以需要开启事务
     *
     * @param comment 封装好的comment对象
     * @return 返回持久层的结果
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        // 1. 对评论（回复）内容进行HTML格式转换和敏感词过滤
        String content = comment.getContent();
        content = HtmlUtils.htmlEscape(content);
        content = sensitiveFilter.filter(content);
        comment.setContent(content);
        // 2. 将comment对象中的数据添加到comment表
        int res = commentMapper.insertComment(comment);

        // 3. 如果是评论帖子的话，修改discuss_post表中对应的comment_count项
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            int count = commentMapper.selectCountByEntity(ENTITY_TYPE_POST, comment.getEntityId());
            discussPostMapper.updateCommentCount(comment.getEntityId(), count);
        }

        return res;
    }

    /**
     * 通过id找到对应的comment对象
     * @param id 评论id
     * @return 返回找到的对应的comment对象
     */
    public Comment findCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }

    /**
     * 通过用户id找到用户发表过的评论
     * @param userId 用户id
     * @param offset 偏移
     * @param limit 每页大小
     * @return 返回找到的评论列表
     */
    public List<Comment> findCommentByUserId(int userId, int offset, int limit) {
        return commentMapper.seletCommentsByUserId(userId, offset, limit);
    }

    /**
     * 通过用户id查找用户发表的评论数
     * @param userId 用户id
     * @return 返回用户id的评论数
     */
    public int findCommentCountByUserId(int userId) {
        return commentMapper.selectCountByUserId(userId);
    }
}

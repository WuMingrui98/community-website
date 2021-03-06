package com.wmr.community.dao;

import com.wmr.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    List<Comment> selectComments(int entityType, int entityId, int offset, int limit);

    List<Comment> seletCommentsByUserId(int userId, int offset, int limit);

    Comment selectCommentById(int id);

    int selectCountByEntity(int entityType, int entityId);

    int selectCountByUserId(int userId);

    int insertComment(Comment comment);
}

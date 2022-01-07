package com.wmr.community.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Setter
@Getter
@ToString
public class Comment {
    private int id;
    private int userId;
    // 1表示评论 2表示回复
    private int entityType;
    // 如果entityType=1时，表示当前Comment对象所在的帖子的id
    // 如果entityType=2时，表示回复的评论的id
    private int entityId;
    // 表示回复对象的id，为0时表示没有回复对象
    private int targetId;
    private String content;
    private int status;
    private Date createTime;
}

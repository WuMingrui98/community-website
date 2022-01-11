package com.wmr.community.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Setter
@Getter
@ToString
public class Message {
    // 私信id
    private int id;
    // 发送人, fromId=1表示是系统发送的
    private int fromId;
    // 接收人
    private int toId;
    // 会话编号，两个人会话的编号，格式为(用户id(小)_用户id(大))
    private String conversationId;
    // 私信内容
    private String content;
    // 状态(0:未读，1：已读，2：异常)
    private int status;
    // 私信创建时间
    private Date createTime;
}

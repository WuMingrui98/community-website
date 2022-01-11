package com.wmr.community.service;

import com.wmr.community.dao.MessageMapper;
import com.wmr.community.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {
    private MessageMapper messageMapper;

    @Autowired
    public void setMessageMapper(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    /**
     * 通过持久层查询用户的会话列表（会话按照时间由新到旧排序)，针对每个会话只返回一条最新的私信
     * @param userId 用户id
     * @param offset mysql中的offset
     * @param limit mysql中的limit
     * @return 返回查询到的用户会话列表
     */
    public List<Message> findConversations(int userId, int offset, int limit) {
        return messageMapper.selectConversations(userId, offset, limit);
    }

    /**
     * 通过持久层查询用户的会话数量
     * @param userId 用户id
     * @return 返回查询到的用户会话数量
     */
    public int findConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    /**
     * 通过持久层查询某个会话中所包含的私信列表
     * @param conversationId 会话的id
     * @param offset mysql中的offset
     * @param limit mysql中的limit
     * @return 返回查询到的会话私信列表
     */
    public List<Message> findLetters(String conversationId, int offset, int limit) {
        return messageMapper.selectLetters(conversationId, offset, limit);
    }

    /**
     * 通过持久层查询某个会话所包含的私信数量
     * @param conversationId 会话id
     * @return 返回查询到的私信数量
     */
    public int findLetterCount(String conversationId) {
        return messageMapper.selectLetterCount(conversationId);
    }

    /**
     * 通过持久层查询未读私信的数量，conversationId == null时查询当前用户所有的未读消息，!=null查询具体会话的未读消息
     * @param userId 用户id
     * @param conversationId 会话id
     * @return 返回查询到的未读私信的数量
     */
    public int findLetterUnreadCount(int userId, String conversationId) {
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }
}

package com.wmr.community.service;

import com.wmr.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 完成点赞和取消点赞的功能，实现以下小功能
     * 1. 根据entityType和entityId，获取redis数据库中对应的key
     * 2. 当key对应的set中存在userId，则将userId移除，实现取消点赞的功能
     * 3. 当key对应的set中不存在userId，则添加userId，实现点赞的功能
     * @param userId 用户id
     * @param entityType 实体类型 1表示评论 2表示回复
     * @param entityId 实体编号 如果entityType=1时，表示当前Comment对象所在的帖子的id；如果entityType=2时，表示回复的评论的id
     */
    public void like(int userId, int entityType, int entityId) {
        // 根据entityType和entityId，获取redis数据库中对应的key
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(entityLikeKey, userId))) {
            // 当key对应的set中存在userId，则将userId移除，实现取消点赞的功能
            redisTemplate.opsForSet().remove(entityLikeKey, userId);
        } else {
            // 当key对应的set中不存在userId，则添加userId，实现点赞的功能
            redisTemplate.opsForSet().add(entityLikeKey, userId);
        }

    }


    /**
     * 从redis数据库查询实体点赞的数量
     * @param entityType 实体类型 1表示评论 2表示回复
     * @param entityId 实体编号 如果entityType=1时，表示当前Comment对象所在的帖子的id；如果entityType=2时，表示回复的评论的id
     * @return 返回查询到实体点赞数量
     */
    public long findEntityLikeCount(int entityType, int entityId) {
        // 根据entityType和entityId，获取redis数据库中对应的key
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        Long size = redisTemplate.opsForSet().size(entityLikeKey);
        return size == null ? 0 : size;
    }


    /**
     * 查询用户对某实体的点赞状态
     * @param userId 用户id
     * @param entityType 实体类型 1表示评论 2表示回复
     * @param entityId 实体编号 如果entityType=1时，表示当前Comment对象所在的帖子的id；如果entityType=2时，表示回复的评论的id
     * @return 返回1表示点赞，返回0表示未点赞
     */
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        // 根据entityType和entityId，获取redis数据库中对应的key
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        boolean like = Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(entityLikeKey, userId));
        return like ? 1 : 0;
    }




}

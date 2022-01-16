package com.wmr.community.service;

import com.wmr.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class FollowService {
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 实现关注的功能
     * @param userId 用户id
     * @param entityType 实体类型
     * @param entityId 实体id
     */
    public void follow(int userId, int entityType, int entityId) {
        // 需要事务支持
        redisTemplate.execute(new SessionCallback<Object>() {
            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityId, entityType);
                operations.multi();
                redisTemplate.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                redisTemplate.opsForZSet().add(followerKey, userId, System.currentTimeMillis());
                // 提交事务
                operations.exec();
                return null;
            }
        });
    }

    /**
     * 实现取关的功能
     * @param userId 用户id
     * @param entityType 实体类型
     * @param entityId 实体id
     */
    public void unfollow(int userId, int entityType, int entityId) {
        // 需要事务支持
        redisTemplate.execute(new SessionCallback<Object>() {
            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityId, entityType);
                operations.multi();
                redisTemplate.opsForZSet().remove(followeeKey, entityId);
                redisTemplate.opsForZSet().remove(followerKey, userId);
                // 提交事务
                operations.exec();
                return null;
            }
        });
    }

    /**
     * 实现查询用户关注的实体数量的功能
     * @param userId 用户id
     * @param entityType 实体类型
     * @return 返回查询到的用户关注的实体数量
     */
    public long findFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        Long count = redisTemplate.opsForZSet().zCard(followeeKey);
        return count == null ? 0 : count;
    }

    /**
     * 实现查询实体的粉丝数量的功能
     * @param entityId 实体id
     * @param entityType 实体类型
     * @return 返回查询到的实体的粉丝数量
     */
    public long findFollowerCount(int entityId, int entityType) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityId, entityType);
        Long count = redisTemplate.opsForZSet().zCard(followerKey);
        return count == null ? 0 : count;
    }

    /**
     * 实现当前用户是否已关注该实体的功能
     * @param userId 用户id
     * @param entityType 实体类型
     * @param entityId 实体id
     * @return 返回当前用户是否已关注，已关注返回true，未关注返回false
     */
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        Double score = redisTemplate.opsForZSet().score(followeeKey, entityId);
        return score != null;
    }
}

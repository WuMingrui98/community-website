package com.wmr.community.service;

import com.wmr.community.dao.UserMapper;
import com.wmr.community.entity.User;
import com.wmr.community.util.CommunityConstant;
import com.wmr.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService implements CommunityConstant {
    private RedisTemplate<String, Object> redisTemplate;

    private UserMapper userMapper;

    @Autowired
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
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

    /**
     * 实现根据用户id查询用户的关注列表，支持分页功能
     * @param userId 用户id
     * @param offset 偏移量
     * @param limit 显示条数
     * @return 返回查询到的关注列表，并把相关信息进行封装
     */
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        Set<ZSetOperations.TypedTuple<Object>> followeeInfoSet = redisTemplate.opsForZSet().reverseRangeWithScores(followeeKey, offset, offset + limit - 1);
        if (followeeInfoSet == null) return null;
        List<Map<String, Object>> list = new ArrayList<>();
        for (ZSetOperations.TypedTuple<Object> followeeInfo: followeeInfoSet) {
            int followeeId = (Integer) followeeInfo.getValue();
            User followee = userMapper.selectById(followeeId);
            Map<String, Object> map = new HashMap<>();
            map.put("followee", followee);
            map.put("followTime", new Date(Objects.requireNonNull(followeeInfo.getScore()).longValue()));
            list.add(map);
        }
        return list;
    }

    /**
     * 实现根据用户id查询用户的粉丝列表，支持分页功能
     * @param userId 用户id
     * @param offset 偏移量
     * @param limit 显示条数
     * @return 返回查询到的粉丝列表，并把相关信息进行封装
     */
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {
        String followerKey = RedisKeyUtil.getFollowerKey(userId, ENTITY_TYPE_USER);
        Set<ZSetOperations.TypedTuple<Object>> followerInfoSet = redisTemplate.opsForZSet().reverseRangeWithScores(followerKey, offset, offset + limit - 1);
        if (followerInfoSet == null) return null;
        List<Map<String, Object>> list = new ArrayList<>();
        for (ZSetOperations.TypedTuple<Object> followerInfo: followerInfoSet) {
            int followerId = (Integer) followerInfo.getValue();
            User follower = userMapper.selectById(followerId);
            Map<String, Object> map = new HashMap<>();
            map.put("follower", follower);
            map.put("followTime", new Date(Objects.requireNonNull(followerInfo.getScore()).longValue()));
            list.add(map);
        }
        return list;
    }



}

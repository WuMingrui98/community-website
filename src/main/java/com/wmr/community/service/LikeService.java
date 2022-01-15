package com.wmr.community.service;

import com.wmr.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
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
     * 1. 根据entityType和entityId，获取redis数据库中实体点赞对应的key
     *      1.1 当key对应的set中存在userId，则将userId移除，实现取消点赞的功能；
     *      1.2 当key对应的set中不存在userId，则添加userId，实现点赞的功能
     * 2. 根据userId，获得redis中用户获赞对应的key
     *      2.1 取消点赞时，key对应的值-1
     *      2.2 点赞时，key对应的值+1
     * @param userId 当前登录用户id
     * @param entityType 实体类型 1表示评论 2表示回复
     * @param entityId 实体编号 如果entityType=1时，表示当前Comment对象所在的帖子的id；如果entityType=2时，表示回复的评论的id
     * @param entityUserId 作者的id
     */
    public void like(int userId, int entityType, int entityId, int entityUserId) {
        // 需要开启事务，因为需要同时修改redis中的两组键值对
        redisTemplate.execute(new SessionCallback<Object>() {
            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                // 根据entityType和entityId，获取redis数据库中对应的key
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                // 根据userId，获得redis中用户获赞对应的key
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                Boolean member = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
                // 开启事务
                operations.multi();

                if (Boolean.TRUE.equals(member)) {
                    // 当key对应的set中存在userId，则将userId移除，实现取消点赞的功能
                    redisTemplate.opsForSet().remove(entityLikeKey, userId);
                    // 取消点赞时，key对应的值-1
                    redisTemplate.opsForValue().decrement(userLikeKey);
                } else {
                    // 当key对应的set中不存在userId，则添加userId，实现点赞的功能
                    redisTemplate.opsForSet().add(entityLikeKey, userId);
                    //点赞时，key对应的值+1
                    redisTemplate.opsForValue().increment(userLikeKey);
                }

                // 提交事务
                return operations.exec();
            }
        });




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


    /**
     * 根据用户id查询该用户获得的赞的数量
     * @param userId 用户id
     * @return 返回该用户获得的赞的数量
     */
    public int findUserLikeCount(int userId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count;
    }

}

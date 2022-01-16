package com.wmr.community.util;

public class RedisKeyUtil {
    private static final String SPILT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";


    /**
     * 获得某个实体的赞在redis中对应的key
     * like:entity:entityType:entityId -> set(userId)
     *
     * @param entityType 实体类型
     * @param entityId 实体id
     * @return 返回该实体的赞在redis中对应的key
     */
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPILT + entityType + SPILT + entityId;
    }

    /**
     * 获得某个用户获得的赞在redis中对应的key
     * like:user:user -> int
     * @param userId 用户id
     * @return 返回用户所获的赞在redis中对应的key
     */
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPILT + userId;
    }


    /**
     * 获得某个用户关注的实体在redis中对应的key
     * followee:userId:entityType -> zset(entityId, now) 用现在的时间作为有序集合的分数用来排序
     *
     * @param userId 用户id
     * @param entityType 实体类型
     * @return 返回用户关注的实体在redis中对应的key
     */
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPILT + userId + SPILT + entityType;
    }

    /**
     * 获得某个实体拥有的粉丝在redis中对应的key
     * follower:entityId:entityType -> zset(userId,now) 用现在的时间作为有序集合的分数用来排序
     * @param entityId 实体id
     * @param entityType 实体类型
     * @return 返回实体拥有的粉丝在redis中对应的key
     */
    public static String getFollowerKey(int entityId, int entityType) {
        return PREFIX_FOLLOWER + SPILT + entityId + SPILT + entityType;
    }



}

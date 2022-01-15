package com.wmr.community.util;

public class RedisKeyUtil {
    private static final String SPILT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";


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



}

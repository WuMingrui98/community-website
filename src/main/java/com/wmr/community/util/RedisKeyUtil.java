package com.wmr.community.util;

public class RedisKeyUtil {
    private static final String SPILT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";


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
}

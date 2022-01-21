package com.wmr.community.util;

public class RedisKeyUtil {
    private static final String SPILT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_USER = "user";
    private static final String PREFIX_UV = "uv";
    private static final String PREFIX_DAU = "dau";
    private static final String PREFIX_POST = "post";


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
     * like:user:userId -> int
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


    /**
     * 获得登录验证码在redis中对应的key
     * @param owner 验证码的持有者
     * @return 返回登录验证码在redis中对应的key
     */
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPILT + owner;
    }

    /**
     * 获取登录凭证的在redis中对应的key
     * @param ticket 登录凭证
     * @return 返回登录凭证的在redis中对应的key
     */
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPILT + ticket;
    }

    /**
     * 根据用户id获取用户信息在redis中对应的key
     * @param userId 用户id
     * @return 返回用户信息在redis中对应的key
     */
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPILT + userId;
    }


    /**
     * 获得单日UV在redis中对应的key
     * @param date 日期
     * @return 返回单日UV在redis中对应的key
     */
    public static String getUVKey(String date) {
        return PREFIX_UV + SPILT + date;
    }

    /**
     * 获得区间UV在redis中对应的key
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 返回区间UV在redis中对应的key
     */
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPILT + startDate + SPILT + endDate;
    }

    /**
     * 获得单日活跃用户在redis中对应的key
     * @param date 日期
     * @return 返回单日活跃用户在redis中对应的key
     */
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPILT + date;
    }

    /**
     * 获得区间活跃用户在redis中对应的key
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 返回区间活跃用户在redis中对应的key
     */
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPILT + startDate + SPILT + endDate;
    }

    /**
     * 获得需要计算分数帖子所存set在redis中对应的key
     * @return 返回需要计算分数帖子所存set在redis中对应的key
     */
    public static String getPostScoreKey() {
        return PREFIX_POST + SPILT + "score";
    }
}

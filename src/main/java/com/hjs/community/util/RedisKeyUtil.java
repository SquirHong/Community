package com.hjs.community.util;

/**
 * @author hong
 * @create 2023-01-12 16:17
 */

public class RedisKeyUtil {
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";

    //目标
    private static final String PREFIX_FOLLOWEE = "followee";
    //粉丝
    private static final String PREFIX_FOLLOWER = "follower";

    private static final String PREFIX_KAPTCHA = "kaptcha";

    private static final String PREFIX_TICKET = "ticket";

    private static final String PREFIX_USER = "user";

    //某个实体的赞
    //like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    //某个用户的赞
    //like:user:userId -> int
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    //某个用户的关注的实体
    // followee:userId:entityType -> zset(entityId，now)        有序集合按照当前时间来排序
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    //某个实体拥有的“粉丝”
    //follower:entityType:entityId -> zset(userId,now)
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    //登录验证码
    //用随机字符串标记正在登录的用户
    public static String getKaptcha(String onwer) {
        return PREFIX_KAPTCHA + SPLIT + onwer;
    }

    //登录的凭证
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    //用户信息
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }
}

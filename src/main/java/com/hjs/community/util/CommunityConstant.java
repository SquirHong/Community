package com.hjs.community.util;

import lombok.Getter;

/**
 * @author hong
 * @create 2023-01-01 20:27
 */
@Getter
public class CommunityConstant {

    /**
     * 激活成功
     */
    public static final int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    public static final int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    public static final int ACTIVATION_FAILURE = 2;

    /**
     * 默认状态的登录凭证的超时时间
     */
    public static final int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /**
     * 记住状态的登录凭证超时时间
     */
    public static final int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;

    /**
     * 实体类型: 帖子
     */
    public static final int ENTITY_TYPE_POST = 1;

    /**
     * 实体类型: 评论
     */
    public static final int ENTITY_TYPE_COMMENT = 2;

    /**
     * 实体类型: 人
     */
    public static final int ENTITY_TYPE_USER = 3;

    /**
     * 主题：评论
     */
    public static final String TOPIC_COMMENT = "comment";

    /**
     * 主题：点赞
     */
    public static final String TOPIC_LIKE = "like";

    /**
     * 主题：关注
     */
    public static final String TOPIC_FOLLOW = "follow";

}

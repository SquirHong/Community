package com.hjs.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author hong
 * @create 2023-01-10 10:22
 */
@Data
public class Message {
    // TODO: 2023/1/10 应该记录一个更新时间  在查看具体的私信时，更具update_time做select
    private int id;
    private int fromId;
    private int toId;
    private String conversationId;
    private String content;
    private int status;
    private Date createTime;
}

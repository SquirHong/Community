package com.hjs.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author hong
 * @create 2023-01-08 17:12
 */
@Data
public class Comment {

    private int id;
    private int userId;
    private int entityType;
    private int entityId;
    private int targetId;
    private String content;
    private int status;
    private Date createTime;

}

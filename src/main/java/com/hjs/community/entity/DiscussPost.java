package com.hjs.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author hong
 * @create 2022-12-30 17:26
 */
@Data
public class DiscussPost {
    private int id;
    private int userId;
    private String title;
    private String content;
    private int type;
    private int status;
    private Date createTime;
    private int commentCount;
    private double score;
}

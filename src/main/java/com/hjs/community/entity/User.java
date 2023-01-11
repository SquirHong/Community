package com.hjs.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author hong
 * @create 2022-12-29 21:06
 */
@Data
public class User {

    private int id;
    private String username;
    private String password;
    private String salt;
    private String email;
    private int type;
    private int status;
    private String activationCode;
    private String headerUrl;
    private Date createTime;


}

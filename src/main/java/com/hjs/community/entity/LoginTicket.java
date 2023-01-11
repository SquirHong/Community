package com.hjs.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author hong
 * @create 2023-01-02 17:06
 */
@Data
public class LoginTicket {

    private int id;
    private int userId;
    private String ticket;
    private int status;
    private Date expired;
}

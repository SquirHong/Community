package com.hjs.community.dao;

import com.hjs.community.entity.LoginTicket;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.springframework.stereotype.Repository;

/**
 * @author hong
 * @create 2023-01-02 17:07
 */
@Repository
public interface LoginTicketMapper {

    @Insert({
            "insert into login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"
    })
    @Options(useGeneratedKeys = true,keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    LoginTicket selectByTicket(String ticket);


    int updateStatus(String ticket,int status);

}

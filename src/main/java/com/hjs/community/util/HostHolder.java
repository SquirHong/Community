package com.hjs.community.util;

import com.hjs.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 起一个容器的作用 持有用户信息
 * @author hong
 * @create 2023-01-04 18:21
 */
@Component
public class HostHolder {

    ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    public void clear(){
        users.remove();
    }

}

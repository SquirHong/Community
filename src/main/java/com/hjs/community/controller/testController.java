package com.hjs.community.controller;


import com.hjs.community.dao.UserMapper;
import com.hjs.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hong
 * @create 2022-12-28 16:37
 */
@RestController
@RequestMapping("/demo")
public class testController {
    @Autowired
    private UserMapper userMapper;

    @GetMapping("/sayhello")
    public String sayhello(){
        return "hello springBoot.";
    }

    @GetMapping("/getUser/{id}")
    public User getUsers(@PathVariable("id") int id){
        User user = userMapper.selectById(id);
        return user;
    }
}

package com.hjs.community;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@MapperScan({"com.hjs.community.dao"})
public class CommunityApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommunityApplication.class, args);
    }

    // TODO: 2023/1/8 项目存在统一回传数据格式问题  应封装一个专门返回给前端数据的类
    // TODO: 2023/1/10 分页能不能不在mapper.xml里做 ？

}

package com.hjs.community.dao;

import com.hjs.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author hong
 * @create 2022-12-29 21:14
 */
@Repository
public interface UserMapper {

    User selectById(int id);

    User selectByName(String name);

    User selectByEmail(String email);

    int insertUser(User user);

    int insertUsers(List<User> users);

    int updateStatus(int id,int status);

    int updateHeader(int id,String headerUrl);

    int updatePassword(int id,String password);

    //这里 如果用where email = #{email} 的话，索引会失效
//    int updatePassword(String email,String password);
}

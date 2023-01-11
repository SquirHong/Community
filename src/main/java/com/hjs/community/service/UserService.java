package com.hjs.community.service;

import com.hjs.community.dao.LoginTicketMapper;
import com.hjs.community.dao.UserMapper;
import com.hjs.community.entity.LoginTicket;
import com.hjs.community.entity.User;
import com.hjs.community.util.CommunityConstant;
import com.hjs.community.util.CommunityUtil;
import com.hjs.community.util.MailClient;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author hong
 * @create 2022-12-30 19:40
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired(required = false)
    private TemplateEngine templateEngine;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发邮件的时候 要带上 项目名和域名
     */
    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id){
        return userMapper.selectById(id);
    }

    public Map<String,Object> register(User user){
        //map用来返回失败情况
        Map<String,Object> map = new HashMap<>();
        //判空
        if (ObjectUtils.isEmpty(user)){
            throw new IllegalArgumentException("user为空");
        }
        //这里 前端可判空
        if (StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账户不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空");
            return map;
        }
        User u = userMapper.selectByName(user.getUsername());
        if (u != null){
            map.put("usernameMsg","账号已存在");
            return map;
        }
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null){
            map.put("emailMsg","邮箱已被使用");
            return map;
        }

        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //发送邮箱
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        //上面 insert的时候 mybatis已经为次user实例赋值了id
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(),"激活账号",content);

        return map;
    }

    public int activation(int userId,String code){
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1){
            return CommunityConstant.ACTIVATION_REPEAT;
        }else if (user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);
            return CommunityConstant.ACTIVATION_SUCCESS;
        }else{
            return CommunityConstant.ACTIVATION_FAILURE;
        }

    }
    //登录凭证有效时间可由服务器决定，
    public Map<String,Object> login(String username,String password,int expired){
        Map<String,Object> map = new HashMap<>();
        //判空
        if (StringUtils.isBlank(username)){
            map.put("usernameMsg","账号为空");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("passwordMsg","密码为空");
            return map;
        }
        User user = userMapper.selectByName(username);
        if (user == null){
            map.put("usernameMsg","该账号不存在");
            return map;
        }
        if (user.getStatus() == 0){
            map.put("usernameMsg","该账号未激活");
            return map;
        }
        //验证密码
        password = CommunityUtil.md5(password+user.getSalt());
        if (!password.equals(user.getPassword())){
            map.put("passwordMsg","密码不正确");
            return map;
        }
        //增加一条登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+ expired*1000));
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket){
        loginTicketMapper.updateStatus(ticket,1);
    }

    public Map<String,Object> getVerifyCode(String email){
        // TODO: 2023/1/3 改用redis缓存验证码
        Map<String,Object> map = new HashMap<>();
        User user = userMapper.selectByEmail(email);
        if (user == null){
            map.put("emailMsg","该邮箱未注册过");
            return map;
        }
        //发邮件  存储验证码
        String verifyCode = CommunityUtil.generateUUID().substring(0, 5);
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        context.setVariable("verifyCode",verifyCode);
        String content = templateEngine.process("/mail/forget",context);
        map.put("verifyCode",verifyCode);
        mailClient.sendMail(user.getEmail(),"忘记密码",content);
        return map;
    }

    public Map<String,Object> resetPassword(String email, String password) {
        Map<String,Object> map = new HashMap<>();
        if (StringUtils.isBlank(email)){
            map.put("emailMsg","邮箱为空");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("passwordMsg","密码为空");
            return map;
        }
        User user = userMapper.selectByEmail(email);
        if (user == null){
            map.put("emailMsg","该邮箱未注册过");
            return map;
        }
        password = CommunityUtil.md5(password+user.getSalt());
        int i = userMapper.updatePassword(user.getId(), password);
        map.put("user",user);
        return map;
    }

    public LoginTicket getLoginTicket(String ticket){
        return loginTicketMapper.selectByTicket(ticket);
    }

    @Async
    public void updateHeader(int userId,String headerUrl){
        userMapper.updateHeader(userId, headerUrl);
    }

    public Map<String, Object> updatePassword(User user,String oldPassword, String newPassword) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isBlank(oldPassword)){
            map.put("oldPasswordMsg","输入的旧密码为空");
            return map;
        }
        if (StringUtils.isBlank(newPassword)){
            map.put("newPasswordMsg","输入的新密码为空");
            return map;
        }
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if (!oldPassword.equals(user.getPassword())){
            map.put("oldPasswordMsg","旧密码错误");
            return map;
        }
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userMapper.updatePassword(user.getId(),newPassword);
        return map;
    }

    public User getUserByName(String name){
        return userMapper.selectByName(name);
    }
}

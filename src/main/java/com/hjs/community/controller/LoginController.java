package com.hjs.community.controller;

import com.google.code.kaptcha.Producer;
import com.hjs.community.entity.User;
import com.hjs.community.service.UserService;
import com.hjs.community.util.CommunityConstant;
import com.hjs.community.util.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author hong
 * @create 2023-01-01 16:42
 */
@Controller
public class LoginController {

    @Autowired(required = false)
    private Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer producer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @GetMapping("/register")
    public String getRegisterPage(){
        return "/site/register";
    }

    @GetMapping("/login")
    public String getLoginPage(){
        return "/site/login";
    }

    @GetMapping("/forget")
    public String getForgetPage(){
        return "/site/forget";
    }

    @PostMapping("/register")
    public String  register(Model model, User user){
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()){
            model.addAttribute("msg","注册成功，已向您的邮箱发送激活码！");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }
        model.addAttribute("usernameMsg",map.get("usernameMsg"));
        model.addAttribute("passwordMsg",map.get("passwordMsg"));
        model.addAttribute("emailMsg",map.get("emailMsg"));
        return "/site/register";
    }

    @GetMapping("/activation/{userId}/{code}")
    public String activation(Model model,@PathVariable("userId") int userId,@PathVariable("code")String code){
        int activation = userService.activation(userId, code);
        if (activation == CommunityConstant.ACTIVATION_SUCCESS){
            model.addAttribute("msg","激活成功,您的账号已经可以正常使用了!");
            model.addAttribute("target","/login");
        }else if (activation == CommunityConstant.ACTIVATION_REPEAT){
            model.addAttribute("msg","无效操作,该账号已经激活过了!");
            model.addAttribute("target","/index");
        }else{
            model.addAttribute("msg","激活失败,您提供的激活码不正确!");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }

    @GetMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response, HttpSession session){
        //生成验证码
        String text = producer.createText();
        BufferedImage image = producer.createImage(text);
        session.setAttribute("kaptcha",text);
        response.setContentType("image/png");
        try {
            OutputStream outputStream = response.getOutputStream();
            ImageIO.write(image,"png",outputStream);
        } catch (IOException e) {
            logger.error("相应验证码失败"+e.getMessage());
        }
    }

    @PostMapping("/login")
    public String login(String username,String password,String code,boolean rememberme,Model model,HttpSession session,HttpServletResponse response){
        if (StringUtils.isBlank(code) || ! code.equalsIgnoreCase((String) session.getAttribute("kaptcha"))){
            model.addAttribute("codeMsg","验证码不正确");
            return "/site/login";
        }
        // 为 true 的时候 存活 50天  为fasle的时候为 3天
        int expeired = rememberme ? 3600 * 1200 : 3600 * 72;

        Map<String, Object> map = userService.login(username, password, expeired);
        if (map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket", (String) map.get("ticket"));
            cookie.setPath(contextPath);
            cookie.setMaxAge(expeired);
            response.addCookie(cookie);

            return "redirect:/index";
        }else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @GetMapping("/logout")
    public String logout(@CookieValue("ticket") String  ticket){
        userService.logout(ticket);
        return "redirect:/login";
    }

    @GetMapping("/forget/code")
    @ResponseBody
    public String getCode(String email,HttpSession session){
        // TODO: 2023/1/3 待优化代码
        Map<String,Object> map = userService.getVerifyCode(email);
        Object o = map.get("emailMsg");
        if (o != null){
            return CommunityUtil.getJsonString(1, (String) o,null);
        }
        session.setAttribute("verifyCode",map.get("verifyCode"));
        //这里因为 前端 parseJson后需要查看code值是否为0 来做一个 “验证码已发送至您的邮箱”这样一个提示用户功能，所以此处要加responseBody注解
        return CommunityUtil.getJsonString(0,null,null);
    }

    @PostMapping("/forget/password")
    public String resetPassword(String email, String verifyCode, String password, Model model, HttpSession session) {
        String code = (String) session.getAttribute("verifyCode");
        if (StringUtils.isBlank(code) || StringUtils.isBlank(verifyCode) || !code.equalsIgnoreCase(verifyCode)){
            model.addAttribute("emailMsg","验证码错误");
            return "/site/forget";
        }
        Map<String, Object> map = userService.resetPassword(email, password);
        if (map.containsKey("user")){
            return "redirect:/login";
        }else {
            model.addAttribute("emailMsg",map.get("emailMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/forget";
        }

    }

}

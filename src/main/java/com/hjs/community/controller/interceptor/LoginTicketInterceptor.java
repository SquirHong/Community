package com.hjs.community.controller.interceptor;

import com.hjs.community.entity.LoginTicket;
import com.hjs.community.entity.User;
import com.hjs.community.service.UserService;
import com.hjs.community.util.CookieUtil;
import com.hjs.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @author hong
 * @create 2023-01-04 17:09
 */
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    //在controller方法之前执行
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ticket = CookieUtil.getCookie(request, "ticket");
        if (ticket != null){
            LoginTicket loginTicket = userService.getLoginTicket(ticket);
            // TODO: 2023/1/4 这里需要了解一下 浏览器是怎么处理过期的cookie的  && loginTicket.getExpired().after(new Date())
            if (loginTicket != null && loginTicket.getStatus() == 0 ){
                User user = userService.findUserById(loginTicket.getUserId());
                hostHolder.setUser(user);
            }
        }
        return true;
    }

    //在controller方法后执行
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null){
            modelAndView.addObject("loginUser",user);
        }
    }

    //在渲染页面之前执行
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}

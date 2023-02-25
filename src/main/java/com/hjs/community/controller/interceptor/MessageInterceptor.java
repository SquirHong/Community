package com.hjs.community.controller.interceptor;

import com.hjs.community.entity.User;
import com.hjs.community.service.MessageService;
import com.hjs.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author hong
 * @create 2023-02-25 22:52
 */
@Component
public class MessageInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            int letterUnReadCount = messageService.findUnreadLetterCount(user.getId(), null);
            int noticeUnreadCount = messageService.findNoticeUnreadCountIf(user.getId());
            modelAndView.addObject("allUnreadCount", letterUnReadCount + noticeUnreadCount);
        }


    }
}

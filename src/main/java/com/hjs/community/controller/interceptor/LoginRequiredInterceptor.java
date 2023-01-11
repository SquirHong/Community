package com.hjs.community.controller.interceptor;

import com.hjs.community.annotation.LoginRequired;
import com.hjs.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 拦截非登录状态区访问跨权限请求
 * @author hong
 * @create 2023-01-06 5:47
 */
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // TODO: 2023/1/6 考虑 拦截器已在webMvcConfig里进行了配置  而在配置中 已将 所有静态资源默认放行，这里是否不用判断是否目标对象是否是一个方法
        //判断请求是否是controller，如果是静态资源 可直接访问
        if (handler instanceof HandlerMethod){
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            if (loginRequired != null && hostHolder.getUser() == null){
                //未登录状态 被拦截到应该去登录页面
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
        }
        return true;
    }


}

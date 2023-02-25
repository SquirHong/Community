package com.hjs.community.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 统一记录日志
 *
 * @author hong
 * @create 2023-01-12 10:38
 */

@Component
@Aspect
public class ServiceLogAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceLogAspect.class);

    @Pointcut("execution(* com.hjs.community.service.*.*(..))")
    public void pointcut() {
    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        // 用户[ip],在[now],访问了[com.nowcoder.community.service.xxx()].
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        //这里因为  kafka的consumer 消费信息 会调用 messageService的添加message方法，此请求是由 kafka内部调用，所以并不包含request
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getRemoteHost();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        LOGGER.info(String.format("用户[%s],在[%s],访问了[%s].", ip, now, target));

    }

}

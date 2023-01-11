package com.hjs.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 起一个表示作用，然后让拦截器统一处理
 * @author hong
 * @create 2023-01-06 5:43
 */
//该注解可写在哪
@Target(ElementType.METHOD)
//该注解有效范围
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {
}

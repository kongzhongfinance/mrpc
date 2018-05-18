package com.kongzhong.mrpc.interceptor;

/**
 * 拦截器执行器
 * <p>
 * Created by biezhi on 2016/12/23.
 */
public interface Invocation {

    Object next() throws Throwable;

}
package com.kongzhong.mrpc.admin.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 基础拦截器
 *
 * @author biezhi
 *         2017/5/2
 */
@Slf4j
public class BaseInterceptor extends HandlerInterceptorAdapter {

    @Value("${app.version:'0.0.1'}")
    private String version;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();

        log.info("[{}\t{}]", httpMethod, requestURI);
        request.setAttribute("version", version);
        return true;
    }

}

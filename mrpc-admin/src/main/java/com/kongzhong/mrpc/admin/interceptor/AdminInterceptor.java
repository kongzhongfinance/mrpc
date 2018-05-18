package com.kongzhong.mrpc.admin.interceptor;

import com.kongzhong.mrpc.admin.config.RpcAdminProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.kongzhong.mrpc.admin.config.RpcAdminConst.LOGIN_SESSION_KEY;

/**
 * 基础拦截器
 *
 * @author biezhi
 *         2017/5/2
 */
@Slf4j
public class AdminInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private RpcAdminProperties adminProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();

        log.info("{}\t{}", httpMethod, requestURI);
        request.setAttribute("version", adminProperties.getVersion());

//        if (requestURI.startsWith("/admin")) {
//            Object loginModel = request.getSession().getAttribute(LOGIN_SESSION_KEY);
//            if (null == loginModel) {
//                response.sendRedirect("/signin");
//                return false;
//            } else {
//
//            }
//        }
        return true;
    }

}

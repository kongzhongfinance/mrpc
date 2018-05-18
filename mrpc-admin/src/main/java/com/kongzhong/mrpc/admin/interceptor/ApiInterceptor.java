package com.kongzhong.mrpc.admin.interceptor;

import com.kongzhong.mrpc.admin.config.RpcAdminProperties;
import com.kongzhong.mrpc.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import sun.misc.BASE64Decoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * API拦截器
 *
 * @author biezhi
 *         2017/5/2
 */
@Slf4j
public class ApiInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private RpcAdminProperties adminProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();

        log.info("{}\t{}", httpMethod, requestURI);

        Object sessionAuth = request.getSession().getAttribute("basic_auth");
        if (sessionAuth != null) {
            return true;
        }

        if (!checkHeaderAuth(request)) {
            response.setStatus(401);
            response.setHeader("Cache-Control", "no-store");
            response.setDateHeader("Expires", 0);
            response.setHeader("WWW-authenticate", "Basic Realm=\"test\"");
            return false;
        }
        return true;
    }

    private boolean checkHeaderAuth(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        log.debug("Authorization: {}", auth);

        if (StringUtils.isNotEmpty(auth) && auth.length() > 6) {
            auth = auth.substring(6, auth.length());

            String decodedAuth = getFromBASE64(auth);
            log.debug("Authorization decode: {}", decodedAuth);

            String[] arr = decodedAuth.split(":");
            if (arr.length == 2) {
                if (adminProperties.getUser().equals(arr[0]) && adminProperties.getPass().equals(arr[1])) {
                    request.getSession().setAttribute("basic_auth", decodedAuth);
                    return true;
                }
            }
        }

        return false;
    }

    private String getFromBASE64(String s) {
        if (s == null)
            return null;
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            byte[] b = decoder.decodeBuffer(s);
            return new String(b);
        } catch (Exception e) {
            return null;
        }
    }

}

package com.kongzhong.mrpc.trace.interceptor;

import com.kongzhong.mrpc.trace.config.TraceClientAutoConfigure;
import com.kongzhong.mrpc.trace.utils.Exclusions;
import com.kongzhong.mrpc.trace.utils.ServletPathMatcher;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 * @author biezhi
 * @date 2017/11/29
 */
@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class TraceMvcInterceptor extends HandlerInterceptorAdapter {

    private BaseFilter baseFilter;
    private Set<String>        excludesPattern = Exclusions.defaultExclusions().getExclusions();
    private ServletPathMatcher pathMatcher     = ServletPathMatcher.getInstance();

    public TraceMvcInterceptor(TraceClientAutoConfigure clientAutoConfigure) {
        baseFilter = new BaseFilter(clientAutoConfigure);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (!baseFilter.enabled()) {
            return true;
        }

        String uri = request.getRequestURI();
        if (isExclusion(request.getContextPath(), uri)) {
            return true;
        }

        // start root span
        baseFilter.startTrace(request);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        super.afterCompletion(request, response, handler, ex);
        // end root span
        baseFilter.endTrace(request);
    }

    private boolean isExclusion(String contextPath, String requestURI) {
        if (excludesPattern == null || requestURI == null) {
            return false;
        }

        if (contextPath != null && requestURI.startsWith(contextPath)) {
            requestURI = requestURI.substring(contextPath.length());
            if (!requestURI.startsWith("/")) {
                requestURI = "/" + requestURI;
            }
        }

        for (String pattern : excludesPattern) {
            if (pathMatcher.matches(pattern, requestURI)) {
                return true;
            }
        }
        return false;
    }


}

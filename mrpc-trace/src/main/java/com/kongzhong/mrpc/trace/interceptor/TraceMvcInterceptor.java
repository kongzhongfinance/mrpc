package com.kongzhong.mrpc.trace.interceptor;

import com.kongzhong.mrpc.trace.config.TraceAutoConfigure;
import com.kongzhong.mrpc.trace.utils.Exclusions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author biezhi
 * @date 2017/11/29
 */
@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class TraceMvcInterceptor extends HandlerInterceptorAdapter {

    private BaseFilter baseFilter;

    public TraceMvcInterceptor(TraceAutoConfigure clientAutoConfigure) {
        baseFilter = new BaseFilter(clientAutoConfigure);
        baseFilter.setExcludesPattern(Exclusions.defaultExclusions().getExclusions());
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!baseFilter.enabled() || baseFilter.isExclusion(request)) {
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


}

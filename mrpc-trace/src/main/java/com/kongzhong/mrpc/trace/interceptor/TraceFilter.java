package com.kongzhong.mrpc.trace.interceptor;

import com.kongzhong.basic.zipkin.TraceContext;
import com.kongzhong.basic.zipkin.agent.AbstractAgent;
import com.kongzhong.mrpc.trace.config.TraceAutoConfigure;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

/**
 * TraceFilter
 */
@Slf4j
public class TraceFilter implements Filter {

    private static final String PARAM_NAME_EXCLUSIONS = "exclusions";
    private BaseFilter baseFilter;

    public TraceFilter(TraceAutoConfigure clientAutoConfigure, AbstractAgent agent) {
        this.baseFilter = new BaseFilter(clientAutoConfigure);
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        String exclusions = config.getInitParameter(PARAM_NAME_EXCLUSIONS);
        if (exclusions != null && exclusions.trim().length() != 0) {
            baseFilter.setExcludesPattern(new HashSet<>(Arrays.asList(exclusions.split("\\s*,\\s*"))));
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        if (!baseFilter.enabled() || baseFilter.isExclusion(req)) {
            chain.doFilter(request, response);
            return;
        }

        baseFilter.startTrace(req);
        // executor other filters
        chain.doFilter(request, response);
        // end root span
        baseFilter.endTrace(req);
    }

    @Override
    public void destroy() {
        // clear trace context
        TraceContext.clear();
    }


}
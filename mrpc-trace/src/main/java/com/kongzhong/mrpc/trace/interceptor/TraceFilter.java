package com.kongzhong.mrpc.trace.interceptor;

import com.kongzhong.basic.zipkin.TraceContext;
import com.kongzhong.basic.zipkin.agent.AbstractAgent;
import com.kongzhong.mrpc.trace.config.TraceClientAutoConfigure;
import com.kongzhong.mrpc.trace.utils.ServletPathMatcher;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * TraceFilter
 */
@Slf4j
public class TraceFilter implements Filter {

    private static final String PARAM_NAME_EXCLUSIONS = "exclusions";
    private BaseFilter  baseFilter;
    private Set<String> excludesPattern;
    private String      contextPath;

    /**
     * PatternMatcher used in determining which paths to react to for a given request.
     */
    private ServletPathMatcher pathMatcher = ServletPathMatcher.getInstance();

    public TraceFilter(TraceClientAutoConfigure clientAutoConfigure, AbstractAgent agent) {
        this.baseFilter = new BaseFilter(clientAutoConfigure);
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        String exclusions = config.getInitParameter(PARAM_NAME_EXCLUSIONS);
        if (exclusions != null && exclusions.trim().length() != 0) {
            excludesPattern = new HashSet<>(Arrays.asList(exclusions.split("\\s*,\\s*")));
        }
        this.contextPath = getContextPath(config.getServletContext());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!baseFilter.enabled()) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest req = (HttpServletRequest) request;
        String             uri = req.getRequestURI();

        if (isExclusion(uri)) {
            chain.doFilter(request, response);
            return;
        }


        baseFilter.startTrace(req);

        // executor other filters
        chain.doFilter(request, response);

        // end root span
        baseFilter.endTrace(req);
    }

    public boolean isExclusion(String requestURI) {
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

    @Override
    public void destroy() {
        // clear trace context
        TraceContext.clear();
    }

    private static String getContextPath_2_5(ServletContext context) {
        String contextPath = context.getContextPath();

        if (contextPath == null || contextPath.length() == 0) {
            contextPath = "/";
        }

        return contextPath;
    }

    private static String getContextPath(ServletContext context) {
        if (context.getMajorVersion() == 2 && context.getMinorVersion() < 5) {
            return null;
        }
        try {
            return getContextPath_2_5(context);
        } catch (NoSuchMethodError error) {
            return null;
        }
    }


}
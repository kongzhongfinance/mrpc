package com.kongzhong.mrpc.ktrace.interceptor;

import com.kongzhong.finance.ktrace.core.Trace;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * Created by IFT8 on 2017/8/1.
 */
@Slf4j
public class TraceFilter implements Filter {

    @Override
    public void init(FilterConfig config) throws ServletException {
        log.info("init the ktrace interceptor with config({}).", new Object[]{config});
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // do trace
        String currentTraceId = Trace.getCurrentRequestId();
        if (log.isDebugEnabled()) {
            log.debug("TraceFilter CurrentTraceId={}", currentTraceId);
        }

        // executor other filters
        chain.doFilter(request, response);

        // clear trace context
        Trace.continueTrace(null);
    }

    @Override
    public void destroy() {
        // clear trace context
        Trace.continueTrace(null);
    }

}
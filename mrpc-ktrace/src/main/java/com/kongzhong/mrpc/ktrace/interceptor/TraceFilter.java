package com.kongzhong.mrpc.ktrace.interceptor;

import com.kongzhong.finance.ktrace.core.Trace;
import com.kongzhong.mrpc.trace.TraceConstants;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

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
        // clear trace context
        Trace.continueTrace(null);
        //rpc使用MDC方式接入 必须手动清空
        MDC.remove(TraceConstants.TRACE_ID);

        // do trace
        if (log.isDebugEnabled()) {
            String currentTraceId = Trace.getCurrentRequestId();
            log.debug("TraceFilter CurrentTraceId={}", currentTraceId);
        }

        // executor other filters
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // clear trace context
        Trace.continueTrace(null);
        //rpc使用MDC方式接入 必须手动清空
        MDC.remove(TraceConstants.TRACE_ID);
    }

}
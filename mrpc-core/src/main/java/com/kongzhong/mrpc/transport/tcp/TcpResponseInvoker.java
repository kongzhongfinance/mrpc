package com.kongzhong.mrpc.transport.tcp;

import com.kongzhong.mrpc.model.RpcContext;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.model.ServiceBean;
import com.kongzhong.mrpc.server.AbstractResponseInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Tcp响应回调处理
 */
public class TcpResponseInvoker extends AbstractResponseInvoker<Boolean> {

    public static final Logger log = LoggerFactory.getLogger(TcpResponseInvoker.class);

    public TcpResponseInvoker(RpcRequest request, RpcResponse response, Map<String, ServiceBean> serviceBeanMap) {
        super(request, response, serviceBeanMap);
    }

    @Override
    public Boolean call() throws Exception {
        try {
            response.setRequestId(request.getRequestId());
            Object result = super.invokeMethod(request);
            response.setResult(result);
            response.setSuccess(true);
            return Boolean.TRUE;
        } catch (Throwable e) {
            e = this.buildErrorResponse(e, response);
            log.error("Rpc method processor error", e);
            return Boolean.TRUE;
        } finally {
            RpcContext.remove();
        }
    }

}
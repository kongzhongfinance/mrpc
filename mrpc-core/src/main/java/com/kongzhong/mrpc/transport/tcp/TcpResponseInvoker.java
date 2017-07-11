package com.kongzhong.mrpc.transport.tcp;

import com.kongzhong.mrpc.model.*;
import com.kongzhong.mrpc.server.AbstractResponseInvoker;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Tcp响应回调处理
 */
@Slf4j
public class TcpResponseInvoker extends AbstractResponseInvoker<Boolean> {

    TcpResponseInvoker(RpcRequest request, RpcResponse response, Map<String, ServiceBean> serviceBeanMap) {
        super(request, response, serviceBeanMap);
    }

    @Override
    public Boolean call() throws Exception {
        try {
            response.setRequestId(request.getRequestId());
            Object result = super.invokeMethod(request);
            response.setResult(result);
            response.setSuccess(true);
            ServiceStatusTable.me().addSuccessInvoke(request.getClassName());
            return Boolean.TRUE;
        } catch (Throwable e) {
            e = this.buildErrorResponse(e, response);
            log.error("Service method invoke error", e);
            ServiceStatusTable.me().addErrorInvoke(request.getClassName());
            return Boolean.TRUE;
        } finally {
            RpcContext.remove();
        }
    }

}
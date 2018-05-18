package com.kongzhong.mrpc.hystrix;

import com.kongzhong.mrpc.client.invoke.ClientInvocation;
import com.kongzhong.mrpc.interceptor.RpcClientInterceptor;
import com.kongzhong.mrpc.utils.StringUtils;
import org.springframework.core.annotation.Order;

/**
 * @author biezhi
 * @date 2017/7/26
 */
@Order(0)
public class HystrixInterceptor implements RpcClientInterceptor {

    @Override
    public Object execute(ClientInvocation invocation) throws Throwable {
        if (StringUtils.isNotEmpty(invocation.getRpcInvoker().getRequest().getFallbackType())) {
            // 是否配置降级
            RpcHystrixCommand command = new RpcHystrixCommand(invocation.getRpcInvoker());
            return command.execute();
        }
        return invocation.next();
    }

}

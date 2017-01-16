package junicorn.mrpc.demo.server;

import junicorn.mrpc.inteceptor.RpcInteceptor;
import junicorn.mrpc.inteceptor.RpcInvocation;

/**
 * Created by biezhi on 2016/12/23.
 */
public class TestInterceptor implements RpcInteceptor {


    @Override
    public Object execute(RpcInvocation invocation) throws Exception {
        System.out.println("interceptor.........");
        return invocation.next();
    }
}

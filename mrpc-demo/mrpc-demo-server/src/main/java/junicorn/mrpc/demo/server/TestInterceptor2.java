package junicorn.mrpc.demo.server;

import junicorn.mrpc.inteceptor.RpcInteceptor;
import junicorn.mrpc.inteceptor.RpcInvocation;

/**
 * Created by biezhi on 2016/12/23.
 */
public class TestInterceptor2 implements RpcInteceptor {


    @Override
    public Object execute(RpcInvocation invocation) throws Exception {
        System.out.println("interceptor2.........");
        return invocation.next();
    }
}

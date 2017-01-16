package junicorn.mrpc.client.proxy;

import junicorn.mrpc.async.AsyncCallProxy;
import junicorn.mrpc.async.RpcFuture;
import junicorn.mrpc.common.model.RpcRequest;
import junicorn.mrpc.common.utils.UUIDUtil;
import junicorn.mrpc.connection.Connection;
import junicorn.mrpc.loadbalance.LoadBalance;
import junicorn.mrpc.loadbalance.SampleLoadBalance;
import junicorn.mrpc.loadbalance.Strategy;

import java.lang.reflect.Method;

/**
 * Created by biezhi on 2016/12/12.
 */
public abstract class AbstractProxy implements AsyncCallProxy {

    private LoadBalance loadBalance = new SampleLoadBalance();
    private Strategy strategy;

    public AbstractProxy(Strategy strategy){
        this.strategy = strategy;
    }

    public abstract <T> T createBean(Class<T> type) throws Exception;

    public abstract RpcFuture call(String funcName, Object... args);

    Object invoke(Method method, Object...args) throws Throwable {
        method.setAccessible(true);
        final RpcRequest request = new RpcRequest(UUIDUtil.getUUID(),
                method.getDeclaringClass().getName(),
                method.getName(),
                method.getParameterTypes(), args);

        try {
            final Connection connection = loadBalance.getConnection(strategy);
            RpcFuture rpcFuture = connection.write(request);
            return rpcFuture.get();
//            Callable<Object> callable = new Callable<Object>() {
//                @Override
//                public Object call() throws Exception {
//                    RpcFuture rpcCallBack = connection.write(request);
//                    return rpcCallBack.get();
//                }
//            };
//            return RpcClient.submit(callable).get();
        } catch (Exception e){
            throw e.getCause();
        }
    }
}

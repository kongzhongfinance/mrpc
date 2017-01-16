package junicorn.mrpc.client.proxy;

import com.google.common.collect.Sets;
import junicorn.mrpc.async.AsyncCallProxy;
import junicorn.mrpc.async.RpcFuture;
import junicorn.mrpc.client.RpcClient;
import junicorn.mrpc.common.config.Constant;
import junicorn.mrpc.common.model.RpcRequest;
import junicorn.mrpc.common.utils.StringUtil;
import junicorn.mrpc.common.utils.UUIDUtil;
import junicorn.mrpc.connection.ConnManager;
import junicorn.mrpc.connection.Connection;
import junicorn.mrpc.loadbalance.LoadBalance;
import junicorn.mrpc.loadbalance.SampleLoadBalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Created by biezhi on 2016/12/12.
 */
public abstract class AbstractProxy implements AsyncCallProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractProxy.class);

    private LoadBalance loadBalance = new SampleLoadBalance();
    private RpcClient rpcClient;

    public AbstractProxy(RpcClient rpcClient){
        this.rpcClient = rpcClient;
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

            int retry = Constant.RPC_RETRY_COUNT;
            RpcFuture rpcFuture = getRpcFuture(request);
            while (null == rpcFuture && retry > 0){
                TimeUnit.SECONDS.sleep(1);
                rpcFuture = getRpcFuture(request);
                retry--;
            }
            if(null != rpcFuture){
                return rpcFuture.get();
            }
            if(StringUtil.isNotEmpty(rpcClient.getServerAddr())){
                ConnManager.updateNodes(Sets.newHashSet(rpcClient.getServerAddr()));
            }
            return null;
//            Callable<Object> callable = new Callable<Object>() {
//                @Override
//                public Object call() throws Exception {
//                    RpcFuture rpcCallBack = connection.write(request);
//                    return rpcCallBack.get();
//                }
//            };
//            return RpcClient.submit(callable).get();
        } catch (Throwable e){
            throw e.getCause();
        }
    }

    private RpcFuture getRpcFuture(RpcRequest request) throws Exception {
        final Connection connection = loadBalance.getConnection(rpcClient.getStrategy());
        return connection.write(request);
    }
}

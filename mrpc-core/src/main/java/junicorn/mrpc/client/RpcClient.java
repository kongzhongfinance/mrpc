package junicorn.mrpc.client;

import com.google.common.collect.Sets;
import junicorn.mrpc.async.AsyncCallProxy;
import junicorn.mrpc.client.proxy.AbstractProxy;
import junicorn.mrpc.client.proxy.CglibProxy;
import junicorn.mrpc.common.utils.RpcThreadPool;
import junicorn.mrpc.connection.ConnManager;
import junicorn.mrpc.loadbalance.Strategy;
import junicorn.mrpc.registry.ServiceDiscovery;
import junicorn.mrpc.serialize.RpcSerialize;
import junicorn.mrpc.serialize.RpcSerializeBuilder;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class RpcClient {

    private ServiceDiscovery serviceDiscovery;

    private static final ThreadPoolExecutor threadPoolExecutor = RpcThreadPool.getThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2, Integer.MAX_VALUE);

    /**
     * default loadbalance strategy
     */
    private Strategy strategy = Strategy.POLL;

    static RpcSerialize rpcSerialize;

    private AbstractProxy proxy = new CglibProxy(strategy);

    public RpcClient(String serialize, String serverAddr) {
        this.setSerialize(serialize);
        ConnManager.updateNodes(Sets.newHashSet(serverAddr));
    }

    public RpcClient(String serialize, ServiceDiscovery serviceDiscovery) {
        this.setSerialize(serialize);
        this.serviceDiscovery = serviceDiscovery;
    }

    public <T> T create(Class<T> interfaceClass) {
        try {
            return proxy.createBean(interfaceClass);
        } catch (Exception e){
            return null;
        }
    }

    public AsyncCallProxy createAsync(Class<? extends AsyncCallProxy> interfaceClass) {
        try {
            return proxy.createBean(interfaceClass);
        } catch (Exception e){
            return null;
        }
    }

    public static Future<?> submit(Runnable task){
        return threadPoolExecutor.submit(task);
    }

    public static Future<?> submit(Callable task){
        return threadPoolExecutor.submit(task);
    }

    public void stop() {
        if(null != serviceDiscovery){
            serviceDiscovery.stop();
        }
        ConnManager.stop();
        threadPoolExecutor.shutdown();
    }

    public AbstractProxy getProxy() {
        return proxy;
    }

    public RpcClient setProxy(AbstractProxy proxy) {
        this.proxy = proxy;
        return this;
    }

    public RpcClient setStrategy(String strategy) {
        this.strategy = Strategy.valueOf(strategy.toUpperCase());
        return this;
    }

    public void setSerialize(String serialize) {
        rpcSerialize = RpcSerializeBuilder.build(serialize);
    }

}
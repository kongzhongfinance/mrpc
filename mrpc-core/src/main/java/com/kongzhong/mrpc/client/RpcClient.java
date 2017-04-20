package com.kongzhong.mrpc.client;

import com.google.common.reflect.Reflection;
import com.kongzhong.mrpc.registry.ServiceDiscovery;
import com.kongzhong.mrpc.serialize.RpcSerialize;

public class RpcClient {

    private RpcServerLoader loader = RpcServerLoader.me();

    private String serverAddr;

    private RpcSerialize rpcSerialize;

    private ServiceDiscovery serviceDiscovery;

    public RpcClient() {

    }

    public RpcClient(String serverAddr) {
        this.setServerAddr(serverAddr);
    }

    public RpcClient(ServiceDiscovery serviceDiscovery) {
        this.setServiceDiscovery(serviceDiscovery);
    }

    public void stop() {
        loader.unLoad();
    }

    /***
     * 动态代理,获得代理后的对象
     * @param rpcInterface
     * @param <T>
     * @return
     */
    public <T> T execute(Class<T> rpcInterface) {
        return (T) Reflection.newProxy(rpcInterface, new ClientProxy<T>());
    }

    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
        this.loader.load(serverAddr);
    }

    public RpcSerialize getRpcSerialize() {
        return rpcSerialize;
    }

    public void setRpcSerialize(RpcSerialize rpcSerialize) {
        this.rpcSerialize = rpcSerialize;
    }

    public ServiceDiscovery getServiceDiscovery() {
        return serviceDiscovery;
    }

    public void setServiceDiscovery(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
        this.loader.load(serviceDiscovery);
    }
}

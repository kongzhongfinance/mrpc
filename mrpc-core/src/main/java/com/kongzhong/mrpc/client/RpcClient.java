package com.kongzhong.mrpc.client;

import com.google.common.reflect.Reflection;
import com.kongzhong.mrpc.enums.SerializeEnum;
import com.kongzhong.mrpc.enums.TransportEnum;
import com.kongzhong.mrpc.registry.ServiceDiscovery;
import com.kongzhong.mrpc.transport.TransferSelector;

/**
 * rpc客户端
 */
public class RpcClient {

    private RpcServerLoader loader = RpcServerLoader.me();

    /**
     * rpc服务地址
     */
    private String serverAddr;

    /**
     * 序列化类型，默认protostuff
     */
    private String serialize = SerializeEnum.PROTOSTUFF.name();

    /**
     * 传输协议，默认tcp协议
     */
    private String transport = TransportEnum.TCP.name();

    /**
     * 服务发现
     */
    private ServiceDiscovery serviceDiscovery;

    /**
     * 传输选择
     */
    private TransferSelector transferSelector = new TransferSelector();

    private boolean isLoad;

    public RpcClient() {
    }

    public RpcClient(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public RpcClient(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
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
    public <T> T getProxyBean(Class<T> rpcInterface) {
        if (!isLoad) {
            this.loader.init(serialize, transport);
            this.loader.load(serverAddr);
            isLoad = true;
        }
        return (T) Reflection.newProxy(rpcInterface, new ClientProxy<T>());
    }

    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public ServiceDiscovery getServiceDiscovery() {
        return serviceDiscovery;
    }

    public void setServiceDiscovery(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    public String getSerialize() {
        return serialize;
    }

    public void setSerialize(String serialize) {
        this.serialize = serialize;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

}
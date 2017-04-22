package com.kongzhong.mrpc.client;

import com.google.common.collect.Sets;
import com.google.common.reflect.Reflection;
import com.kongzhong.mrpc.enums.SerializeEnum;
import com.kongzhong.mrpc.enums.TransportEnum;
import com.kongzhong.mrpc.exception.InitializeException;
import com.kongzhong.mrpc.ha.Connections;
import com.kongzhong.mrpc.model.ClientConfig;
import com.kongzhong.mrpc.registry.ServiceDiscovery;
import com.kongzhong.mrpc.serialize.ProtostuffSerialize;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * rpc客户端
 */
@Data
@NoArgsConstructor
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

    private boolean isLoad;

    public RpcClient(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public RpcClient(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    public void stop() {
        Connections.me().shutdown();
    }

    /***
     * 动态代理,获得代理后的对象
     * @param rpcInterface
     * @param <T>
     * @return
     */
    public <T> T getProxyBean(Class<T> rpcInterface) {
        if (!isLoad) {
            this.init();
        }
        return (T) Reflection.newProxy(rpcInterface, new ClientProxy<T>());
    }

    private void init() {
        synchronized (Connections.class) {
            Connections connections = Connections.me();
            ClientConfig clientConfig = ClientConfig.me();

            RpcSerialize rpcSerialize = null;
            SerializeEnum serializeEnum = SerializeEnum.valueOf(serialize);
            if (null == serializeEnum) {
                throw new InitializeException("serialize type [" + serialize + "] error.");
            }

            if (serializeEnum.equals(SerializeEnum.PROTOSTUFF)) {
                clientConfig.setRpcSerialize(new ProtostuffSerialize());
            }

            TransportEnum transportEnum = TransportEnum.valueOf(transport.toUpperCase());
            if (null == transportEnum) {
                throw new InitializeException("transport type [" + transport + "] error.");
            }
            if (transportEnum.equals(TransportEnum.HTTP)) {
                clientConfig.setHttp(true);
            }
            clientConfig.setTransport(transportEnum);

            if (null == serviceDiscovery) {
                connections.updateNodes(Sets.newHashSet(serverAddr));
//                serverAddr = serviceDiscovery.discover();
            } else {
                serviceDiscovery.discover();
            }
            isLoad = true;
        }
    }

}
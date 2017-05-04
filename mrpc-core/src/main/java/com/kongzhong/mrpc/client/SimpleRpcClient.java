package com.kongzhong.mrpc.client;

import com.google.common.collect.Lists;
import com.google.common.reflect.Reflection;
import com.kongzhong.mrpc.cluster.Connections;
import com.kongzhong.mrpc.cluster.ha.HaStrategy;
import com.kongzhong.mrpc.cluster.loadblance.LBStrategy;
import com.kongzhong.mrpc.config.ClientConfig;
import com.kongzhong.mrpc.config.DefaultConfig;
import com.kongzhong.mrpc.enums.TransportEnum;
import com.kongzhong.mrpc.exception.InitializeException;
import com.kongzhong.mrpc.registry.DefaultDiscovery;
import com.kongzhong.mrpc.registry.ServiceDiscovery;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import com.kongzhong.mrpc.utils.ReflectUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

/**
 * rpc客户端
 */
@Data
@Slf4j
public class SimpleRpcClient {

    /**
     * 序列化类型，默认protostuff
     */
    protected RpcSerialize serialize;

    /**
     * 传输协议，默认tcp协议
     */
    protected String transport;

    /**
     * 服务发现
     */
    protected ServiceDiscovery serviceDiscovery;

    /**
     * 客户端是否已经初始化
     */
    protected boolean isInit;

    /**
     * 负载均衡策略，默认轮询
     */
    protected LBStrategy lbStrategy;

    /**
     * 高可用策略，默认failover
     */
    protected HaStrategy haStrategy;

    /**
     * 引用类名
     */
    protected List<Class<?>> referers = Lists.newArrayList();

    public SimpleRpcClient() {
        this(new DefaultDiscovery());
    }

    public SimpleRpcClient(ServiceDiscovery serviceDiscovery) {
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
        if (!isInit) {
            this.init();
        }
        return (T) Reflection.newProxy(rpcInterface, new ClientProxy<T>());
    }

    private void init() {
        synchronized (Connections.class) {
            Connections connections = Connections.me();
            ClientConfig clientConfig = ClientConfig.me();

            if (null == serialize) {
                serialize = DefaultConfig.serialize();
            }

            if (null == transport) {
                transport = DefaultConfig.transport();
            }

            if (null == lbStrategy) {
                lbStrategy = DefaultConfig.lbStrategy();
            }
            if (null == haStrategy) {
                haStrategy = DefaultConfig.haStrategy();
            }

            if (null == serialize) {
                throw new InitializeException("serialize not is null.");
            }
            TransportEnum transportEnum = TransportEnum.valueOf(transport.toUpperCase());
            if (null == transportEnum) {
                throw new InitializeException("transport type [" + transport + "] error.");
            }
            if (transportEnum.equals(TransportEnum.HTTP)) {
                clientConfig.setHttp(true);
            }

            clientConfig.setRpcSerialize(serialize);
            clientConfig.setLbStrategy(lbStrategy);
            clientConfig.setHaStrategy(haStrategy);
            clientConfig.setTransport(transportEnum);
            clientConfig.setReferers(referers);

            serviceDiscovery.discover();
            isInit = true;
        }
    }

    public void bindReferer(Class<?>... interfaces) {
        if (null != interfaces) {
            referers.addAll(Arrays.asList(interfaces));
        }
    }

    public void bindReferer(String... interfaces) {
        if (null != interfaces) {
            for (String type : interfaces) {
                referers.add(ReflectUtils.from(type));
            }
        }
    }

}
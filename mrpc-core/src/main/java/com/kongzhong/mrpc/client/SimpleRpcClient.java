package com.kongzhong.mrpc.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.Reflection;
import com.kongzhong.mrpc.client.cluster.Connections;
import com.kongzhong.mrpc.client.cluster.HaStrategy;
import com.kongzhong.mrpc.client.cluster.loadblance.LBStrategy;
import com.kongzhong.mrpc.client.proxy.SimpleClientProxy;
import com.kongzhong.mrpc.config.ClientConfig;
import com.kongzhong.mrpc.config.DefaultConfig;
import com.kongzhong.mrpc.enums.TransportEnum;
import com.kongzhong.mrpc.exception.InitializeException;
import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.interceptor.RpcClientInteceptor;
import com.kongzhong.mrpc.interceptor.RpcClientInteceptor;
import com.kongzhong.mrpc.registry.DefaultDiscovery;
import com.kongzhong.mrpc.registry.ServiceDiscovery;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import com.kongzhong.mrpc.utils.ReflectUtils;
import com.kongzhong.mrpc.utils.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * RPC客户端抽象实现
 *
 * @author biezhi
 *         2017/4/25
 */
@Data
@Slf4j
public abstract class SimpleRpcClient {

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
     * appId
     */
    protected String appId;

    /**
     * 直连地址，开发时可配置，当配置了直连则不会走注册中心
     */
    protected String directUrl;

    /**
     * 引用类名
     */
    protected List<Class<?>> referers = Lists.newArrayList();

    protected List<RpcClientInteceptor> inteceptors = Lists.newArrayList();

    public SimpleRpcClient() {
    }

    public SimpleRpcClient(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    /***
     * 动态代理,获得代理后的对象
     *
     * @param rpcInterface
     * @param <T>
     * @return
     */
    public <T> T getProxyBean(Class<T> rpcInterface) {
        if (!isInit) {
            this.init();
        }
        if (StringUtils.isNotEmpty(directUrl)) {
            this.directConnect(directUrl, rpcInterface);
        }
        return (T) Reflection.newProxy(rpcInterface, new SimpleClientProxy<T>(inteceptors));
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
            if (StringUtils.isNotEmpty(appId)) {
                clientConfig.setAppId(appId);
            }
            clientConfig.setHaStrategy(haStrategy);
            clientConfig.setTransport(transportEnum);
            clientConfig.setReferers(referers);

            if (null == directUrl) {
                if (null == serviceDiscovery) {
                    serviceDiscovery = new DefaultDiscovery();
                }
                serviceDiscovery.discover();
            }
            isInit = true;
        }
    }

    /**
     * 直连
     *
     * @param directUrl
     * @param rpcInterface
     */
    private void directConnect(String directUrl, Class<?> rpcInterface) {
        Map<String, Set<String>> mappings = Maps.newHashMap();
        String serviceName = rpcInterface.getName();
        mappings.put(directUrl, Sets.newHashSet(serviceName));
        Connections.me().updateNodes(mappings);
    }

    /**
     * 绑定多个客户端引用服务
     *
     * @param interfaces 接口名
     */
    public void bindReferer(Class<?>... interfaces) {
        if (null != interfaces) {
            referers.addAll(Arrays.asList(interfaces));
        }
    }

    /**
     * 绑定多个客户端引用服务
     *
     * @param interfaces 接口名
     */
    public void bindReferer(String... interfaces) {
        if (null != interfaces) {
            for (String type : interfaces) {
                referers.add(ReflectUtils.from(type));
            }
        }
    }

    public void addInterceptor(RpcClientInteceptor inteceptor) {
        if (null == inteceptor) {
            throw new RpcException("RpcClientInteceptor not is null");
        }
        log.info("Add interceptor [{}]", inteceptor.toString());
        this.inteceptors.add(inteceptor);
    }

    /**
     * 停止客户端，释放资源
     */
    public void stop() {
        Connections.me().shutdown();
    }

}
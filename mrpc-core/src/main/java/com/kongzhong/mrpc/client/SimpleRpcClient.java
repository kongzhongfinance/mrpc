package com.kongzhong.mrpc.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.Reflection;
import com.kongzhong.mrpc.client.cluster.Connections;
import com.kongzhong.mrpc.client.cluster.HaStrategy;
import com.kongzhong.mrpc.client.cluster.ha.FailOverHaStrategy;
import com.kongzhong.mrpc.client.proxy.SimpleClientProxy;
import com.kongzhong.mrpc.config.ClientConfig;
import com.kongzhong.mrpc.config.DefaultConfig;
import com.kongzhong.mrpc.enums.LBStrategy;
import com.kongzhong.mrpc.enums.TransportEnum;
import com.kongzhong.mrpc.exception.InitializeException;
import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.interceptor.RpcClientInteceptor;
import com.kongzhong.mrpc.model.ClientBean;
import com.kongzhong.mrpc.registry.ServiceDiscovery;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import com.kongzhong.mrpc.utils.ReflectUtils;
import com.kongzhong.mrpc.utils.StringUtils;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * RPC客户端抽象实现
 *
 * @author biezhi
 *         2017/4/25
 */
@NoArgsConstructor
@Slf4j
public abstract class SimpleRpcClient {

    /**
     * 序列化类型，默认protostuff
     */
    @Setter
    protected RpcSerialize serialize;

    /**
     * 传输协议，默认tcp协议
     */
    @Setter
    protected String transport;

    /**
     * 客户端是否已经初始化
     */
    protected boolean isInit;

    /**
     * 负载均衡策略，默认轮询
     */
    @Setter
    protected LBStrategy lbStrategy;

    /**
     * 高可用策略，默认failover
     */
    @Setter
    protected HaStrategy haStrategy;

    /**
     * 服务注册实例
     */
    protected Map<String, ServiceDiscovery> serviceDiscoveryMap = Maps.newHashMap();

    /**
     * appId
     */
    @Setter
    protected String appId;

    protected boolean usedRegistry;

    /**
     * 直连地址，开发时可配置，当配置了直连则不会走注册中心
     */
    @Setter
    protected String directAddress;

    /**
     * 引用类名
     */
    @Setter
    protected List<ClientBean> referers = Lists.newArrayList();

    @Setter
    protected List<RpcClientInteceptor> inteceptors = Lists.newArrayList();

    /***
     * 动态代理,获得代理后的对象
     *
     * @param rpcInterface
     * @param <T>
     * @return
     */
    public <T> T getProxyReferer(Class<T> rpcInterface) {
        if (!isInit) {
            try {
                this.init();
            } catch (Exception e) {
                log.error("RPC client init error", e);
            }
        }
        if (StringUtils.isNotEmpty(directAddress)) {
            this.directConnect(directAddress, rpcInterface);
        }
        return this.getProxyBean(rpcInterface);
    }

    public <T> T getProxyBean(Class<T> rpcInterface) {
        return (T) Reflection.newProxy(rpcInterface, new SimpleClientProxy<T>(inteceptors));
    }

    /**
     * 获取服务使用的注册中心
     *
     * @param serviceBean
     * @return
     */
    protected ServiceDiscovery getDiscovery(ClientBean clientBean) {
        ServiceDiscovery serviceDiscovery = serviceDiscoveryMap.get(clientBean.getRegistry());
        return serviceDiscovery;
    }

    protected void init() throws RpcException {
        synchronized (Connections.class) {
            Connections connections = Connections.me();
            ClientConfig clientConfig = ClientConfig.me();

            if (null == serialize) {
                serialize = DefaultConfig.serialize();
            }

            if (null == transport) {
                transport = "tcp";
            }

            if (null == lbStrategy) {
                lbStrategy = LBStrategy.ROUND;
            }
            if (null == haStrategy) {
                haStrategy = new FailOverHaStrategy();
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
            isInit = true;
        }
    }

    /**
     * 直连
     *
     * @param directUrl
     * @param rpcInterface
     */
    protected void directConnect(String directUrl, Class<?> rpcInterface) {
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
            Stream.of(interfaces).forEach(type -> referers.add(new ClientBean(type)));
        }
    }

    public void setDefaultDiscovery(ServiceDiscovery serviceDiscovery) {
        serviceDiscoveryMap.put("default", serviceDiscovery);
    }

    /**
     * 绑定多个客户端引用服务
     *
     * @param interfaces 接口名
     */
    public void bindReferer(String... interfaces) {
        if (null != interfaces) {
            Stream.of(interfaces).forEach(type -> referers.add(new ClientBean(ReflectUtils.from(type))));
        }
    }

    public void addInterceptor(RpcClientInteceptor inteceptor) {
        if (null == inteceptor) {
            throw new IllegalArgumentException("RpcClientInteceptor not is null");
        }
        log.info("Add interceptor [{}]", inteceptor.toString());
        this.inteceptors.add(inteceptor);
    }

    protected void initReferer(ClientBean clientBean, ConfigurableListableBeanFactory beanFactory) {
        String serviceName = clientBean.getServiceName();
        Class<?> serviceClass = clientBean.getServiceClass();
        try {
            Object object = Reflection.newProxy(serviceClass, new SimpleClientProxy<>(inteceptors));
            if (null != beanFactory) {
                beanFactory.registerSingleton(serviceName, object);
            }
            if (usedRegistry) {
                // 服务发现
                ServiceDiscovery serviceDiscovery = this.getDiscovery(clientBean);
                serviceDiscovery.discover();
            } else {
                if (StringUtils.isNotEmpty(this.directAddress)) {
                    this.directConnect(this.directAddress, serviceClass);
                }
            }
            log.info("Bind rpc service [{}]", serviceName);
        } catch (Exception e) {
            log.warn("Bind rpc service [{}] error", serviceName, e);
        }

    }

    /**
     * 停止客户端，释放资源
     */
    public void stop() {
        Connections.me().shutdown();
    }

}
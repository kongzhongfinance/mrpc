package com.kongzhong.mrpc.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.Reflection;
import com.kongzhong.mrpc.client.cluster.Connections;
import com.kongzhong.mrpc.client.cluster.HaStrategy;
import com.kongzhong.mrpc.client.cluster.ha.FailFastHaStrategy;
import com.kongzhong.mrpc.client.cluster.ha.FailOverHaStrategy;
import com.kongzhong.mrpc.client.proxy.SimpleClientProxy;
import com.kongzhong.mrpc.config.ClientConfig;
import com.kongzhong.mrpc.config.NettyConfig;
import com.kongzhong.mrpc.enums.HaStrategyEnum;
import com.kongzhong.mrpc.enums.LbStrategyEnum;
import com.kongzhong.mrpc.enums.RegistryEnum;
import com.kongzhong.mrpc.enums.TransportEnum;
import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.exception.SystemException;
import com.kongzhong.mrpc.interceptor.RpcClientInteceptor;
import com.kongzhong.mrpc.model.ClientBean;
import com.kongzhong.mrpc.model.RegistryBean;
import com.kongzhong.mrpc.registry.DefaultDiscovery;
import com.kongzhong.mrpc.registry.ServiceDiscovery;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import com.kongzhong.mrpc.utils.CollectionUtils;
import com.kongzhong.mrpc.utils.ReflectUtils;
import com.kongzhong.mrpc.utils.StringUtils;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
    protected String serialize;

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
    protected String lbStrategy;

    /**
     * 高可用策略，默认failover
     */
    @Setter
    protected String haStrategy;

    // 跳过服务绑定
    @Setter
    protected Boolean skipBind = false;

    // 客户端服务调用超时，单位/毫秒
    @Setter
    protected int waitTimeout = 10_000;

    // 快速失效重试次数
    @Setter
    protected int failOverRetry = 3;

    // 重试间隔，单位/毫秒 默认每3秒重连一次
    @Setter
    protected int retryInterval = 3000;

    // 重试次数，默认10次
    @Setter
    protected int retryCount = 10;

    /**
     * 服务注册实例
     */
    protected Map<String, ServiceDiscovery> serviceDiscoveryMap = Maps.newHashMap();

    /**
     * 客户端直连地址列表
     */
    protected Map<String, List<ClientBean>> directAddressList = Maps.newHashMap();

    /**
     * appId
     */
    @Setter
    protected String appId;

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

    /**
     * 客户端拦截器列表
     */
    protected List<RpcClientInteceptor> inteceptors = Lists.newArrayList();

    protected NettyConfig nettyConfig;

    /**
     * 获取一个Class的代理对象
     *
     * @param rpcInterface
     * @param <T>
     * @return
     */
    protected <T> T getProxyBean(Class<T> rpcInterface) {
        return (T) Reflection.newProxy(rpcInterface, new SimpleClientProxy<T>(inteceptors));
    }

    /**
     * 获取服务使用的注册中心
     *
     * @param serviceBean
     * @return
     */
    protected ServiceDiscovery getDiscovery(ClientBean clientBean) {
        String registryName = StringUtils.isNotEmpty(clientBean.getRegistry()) ? clientBean.getRegistry() : "default";
        clientBean.setRegistry(registryName);
        ServiceDiscovery serviceDiscovery = serviceDiscoveryMap.get(registryName);
        return serviceDiscovery;
    }

    protected void init() throws RpcException {

        Connections connections = Connections.me();
        if (null == serialize) serialize = "kyro";
        if (null == transport) transport = "tcp";
        if (null == lbStrategy) lbStrategy = LbStrategyEnum.ROUND.name();
        if (null == haStrategy) haStrategy = HaStrategyEnum.FAILOVER.name();

        RpcSerialize rpcSerialize = null;
        if (serialize.equalsIgnoreCase("kyro")) {
            rpcSerialize = ReflectUtils.newInstance("com.kongzhong.mrpc.serialize.KyroSerialize", RpcSerialize.class);
        }
        if (serialize.equalsIgnoreCase("protostuff")) {
            rpcSerialize = ReflectUtils.newInstance("com.kongzhong.mrpc.serialize.ProtostuffSerialize", RpcSerialize.class);
        }

        HaStrategy haStrategy = null;
        if (this.haStrategy.equalsIgnoreCase(HaStrategyEnum.FAILOVER.name())) {
            haStrategy = new FailOverHaStrategy();
        }
        if (this.haStrategy.equalsIgnoreCase(HaStrategyEnum.FAILFAST.name())) {
            haStrategy = new FailFastHaStrategy();
        }

        LbStrategyEnum lbStrategyEnum = LbStrategyEnum.valueOf(this.lbStrategy.toUpperCase());
        TransportEnum transportEnum = TransportEnum.valueOf(this.transport.toUpperCase());

        ClientConfig.me().setAppId(appId);
        ClientConfig.me().setRpcSerialize(rpcSerialize);
        ClientConfig.me().setHaStrategy(haStrategy);
        ClientConfig.me().setLbStrategy(lbStrategyEnum);
        ClientConfig.me().setSkipBind(skipBind);
        ClientConfig.me().setRetryInterval(retryInterval);
        ClientConfig.me().setRetryCount(retryCount);
        ClientConfig.me().setWaitTimeout(waitTimeout);
        ClientConfig.me().setTransport(transportEnum);

        log.info("{}", ClientConfig.me());

        isInit = true;
    }

    /**
     * 直连
     *
     * @param directUrl
     * @param rpcInterface
     */
    protected void directConnect() {
        Map<String, Set<String>> mappings = Maps.newHashMap();
        directAddressList.forEach((directAddress, clientBeans) -> {
            Set<String> serviceNames = clientBeans.stream().map(clientBean -> clientBean.getServiceName()).collect(Collectors.toSet());
            mappings.put(directAddress, serviceNames);
        });
        if (null != nettyConfig) {
            Connections.me().setNettyConfig(nettyConfig);
        }
        Connections.me().asyncConnect(mappings);
    }

    /**
     * 绑定多个客户端引用服务
     *
     * @param interfaces 接口名
     */
    public void bindReferer(Class<?>... interfaces) {
        if (CollectionUtils.isNotEmpty(interfaces)) {
            Stream.of(interfaces).forEach(type -> referers.add(new ClientBean(type)));
        }
    }

    /**
     * 设置默认注册中心
     *
     * @param serviceDiscovery
     */
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

    /**
     * 添加一个客户端拦截器
     *
     * @param inteceptor
     */
    public void addInterceptor(RpcClientInteceptor inteceptor) {
        if (null == inteceptor) {
            throw new IllegalArgumentException("RpcClientInteceptor not is null");
        }
        log.info("Add interceptor [{}]", inteceptor.toString());
        this.inteceptors.add(inteceptor);
    }

    /**
     * 初始化客户端引用
     *
     * @param clientBean
     * @param beanFactory
     */
    protected void initReferer(ClientBean clientBean, ConfigurableListableBeanFactory beanFactory) {
        String serviceName = clientBean.getServiceName();
        Class<?> serviceClass = clientBean.getServiceClass();
        try {
            Object object = this.getProxyBean(serviceClass);
            if (null != beanFactory) {
                beanFactory.registerSingleton(serviceName, object);
            }
            boolean usedRegistry = this.usedRegistry(clientBean);
            if (usedRegistry) {

                // 服务发现
                ServiceDiscovery serviceDiscovery = this.getDiscovery(clientBean);
                if (null == serviceDiscovery) {
                    throw new SystemException(String.format("Client referer [%s] not found registry [%s]", serviceName, clientBean.getRegistry()));
                }
                serviceDiscovery.discover(clientBean);
            } else {
                String directAddress = this.getDirectAddress(clientBean);
                if (StringUtils.isEmpty(directAddress)) {
                    throw new SystemException("Service discovery or direct address must select one.");
                }

                log.debug("Service [{}] direct to [{}]", serviceName, directAddress);
                List<ClientBean> directUrlServices = directAddressList.getOrDefault(directAddress, new ArrayList<>());
                directUrlServices.add(clientBean);
                directAddressList.put(directAddress, directUrlServices);
            }
            log.info("Bind rpc service [{}]", serviceName);
        } catch (Exception e) {
            throw new SystemException(String.format("Bind rpc service [%s] error.", serviceName), e);
        }
    }

    /**
     * 返回引用是否使用注册中心
     *
     * @param clientBean
     * @return
     */
    protected boolean usedRegistry(ClientBean clientBean) {
        if (StringUtils.isNotEmpty(clientBean.getRegistry())) {
            return true;
        }
        if (serviceDiscoveryMap.containsKey("default") && StringUtils.isEmpty(clientBean.getDirectAddress())) {
            return true;
        }
        return false;
    }

    /**
     * 返回客户端的直连地址
     *
     * @param clientBean
     * @return
     */
    protected String getDirectAddress(ClientBean clientBean) {
        String directAddress = StringUtils.isNotEmpty(clientBean.getDirectAddress()) ? clientBean.getDirectAddress() : this.directAddress;
        clientBean.setDirectAddress(directAddress);
        return directAddress;
    }

    protected ServiceDiscovery parseRegistry(RegistryBean registryBean) {
        String type = registryBean.getType();
        if (RegistryEnum.DEFAULT.getName().equals(type)) {
            return new DefaultDiscovery();
        }
        try {
            if (RegistryEnum.ZOOKEEPER.getName().equals(type)) {
                String zkAddr = registryBean.getAddress();
                Object zookeeperDiscovery = Class.forName("com.kongzhong.mrpc.discover.ZookeeperServiceDiscovery").getConstructor(String.class).newInstance(zkAddr);
                ServiceDiscovery serviceDiscovery = (ServiceDiscovery) zookeeperDiscovery;
                return serviceDiscovery;
            }
        } catch (Exception e) {
            throw new SystemException("Parse ServiceDiscovery error", e);
        }
        return null;
    }

    /**
     * 停止客户端，释放资源
     */
    public void shutdown() {
        log.info("Stop mrpc client");
        Connections.me().shutdown();
        serviceDiscoveryMap.values().forEach(serviceDiscovery -> serviceDiscovery.stop());
    }
}
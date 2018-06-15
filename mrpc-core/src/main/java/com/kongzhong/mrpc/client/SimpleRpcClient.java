package com.kongzhong.mrpc.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.Reflection;
import com.kongzhong.mrpc.client.proxy.SimpleClientProxy;
import com.kongzhong.mrpc.config.AdminConfig;
import com.kongzhong.mrpc.config.ClientConfig;
import com.kongzhong.mrpc.config.NettyConfig;
import com.kongzhong.mrpc.enums.HaStrategyEnum;
import com.kongzhong.mrpc.enums.LbStrategyEnum;
import com.kongzhong.mrpc.enums.NodeStatusEnum;
import com.kongzhong.mrpc.enums.RegistryEnum;
import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.exception.SystemException;
import com.kongzhong.mrpc.interceptor.RpcClientInterceptor;
import com.kongzhong.mrpc.model.ClientBean;
import com.kongzhong.mrpc.model.RpcClientNotice;
import com.kongzhong.mrpc.model.RegistryBean;
import com.kongzhong.mrpc.registry.DefaultDiscovery;
import com.kongzhong.mrpc.registry.ServiceDiscovery;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import com.kongzhong.mrpc.serialize.jackson.JacksonSerialize;
import com.kongzhong.mrpc.transport.http.HttpClientHandler;
import com.kongzhong.mrpc.utils.HttpRequest;
import com.kongzhong.mrpc.utils.NetUtils;
import com.kongzhong.mrpc.utils.ReflectUtils;
import com.kongzhong.mrpc.utils.StringUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.kongzhong.mrpc.Const.COMMON_DATE_TIME_FORMATTER;

/**
 * RPC客户端抽象实现
 *
 * @author biezhi
 * 2017/4/25
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
     * 客户端是否已经初始化
     */
    boolean isInit;

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

    /**
     * 跳过服务绑定
     */
    @Setter
    Boolean skipBind = false;

    /**
     * 客户端服务调用超时，单位/毫秒
     */
    @Setter
    protected int waitTimeout = 10_000;

    /**
     * 快速失效重试次数
     */
    @Setter
    protected int failOverRetry = 3;

    /**
     * 客户端断线重连间隔，单位/毫秒 默认每3秒重连一次
     */
    @Setter
    protected int retryInterval = 3000;

    /**
     * 客户端断线重连次数，默认10次
     */
    @Setter
    protected int retryCount = 10;

    /**
     * 客户端ping间隔
     */
    @Setter
    protected int pingInterval = -1;

    /**
     * 后台配置
     */
    @Getter
    @Setter
    protected AdminConfig adminConfig;

    /**
     * 服务注册实例
     */
    protected Map<String, ServiceDiscovery> serviceDiscoveryMap = Maps.newHashMap();

    /**
     * 客户端直连地址列表
     */
    private Map<String, List<ClientBean>> directAddressList = Maps.newHashMap();

    private static final Integer DEFAULT_CLIENT_TIMEOUT = 10000;

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
    protected List<ClientBean> clientBeans = Lists.newArrayList();

    /**
     * 客户端拦截器列表
     */
    private List<RpcClientInterceptor> rpcClientInterceptors = Lists.newArrayList();

    protected static BeanFactory beanFactory;

    protected NettyConfig nettyConfig;

    private volatile boolean isClosed = false;
    private          Lock    lock     = new ReentrantLock();

    /**
     * 获取一个Class的代理对象
     *
     * @param rpcInterface Rpc服务接口
     * @param <T>          服务接口类型
     * @return 返回服务代理类
     */
    <T> T getProxyBean(Class<T> rpcInterface) {
        return Reflection.newProxy(rpcInterface, new SimpleClientProxy(rpcClientInterceptors));
    }

    <T> T getProxyBean(Integer waitTimeout, Class<T> rpcInterface) {
        return Reflection.newProxy(rpcInterface, new SimpleClientProxy(waitTimeout, rpcClientInterceptors));
    }

    /**
     * 获取服务使用的注册中心
     *
     * @param clientBean 客户端引用Bean
     * @return 返回该引用服务发现对象
     */
    private ServiceDiscovery getDiscovery(ClientBean clientBean) {
        String registryName = StringUtils.isNotEmpty(clientBean.getRegistry()) ? clientBean.getRegistry() : "default";
        clientBean.setRegistry(registryName);
        return serviceDiscoveryMap.get(registryName);
    }

    protected void init() throws RpcException {

        if (null == serialize) {
            serialize = "kyro";
        }
        if (null == lbStrategy) {
            lbStrategy = LbStrategyEnum.ROUND.name();
        }
        if (null == haStrategy) {
            haStrategy = HaStrategyEnum.FAILOVER.name();
        }

        RpcSerialize rpcSerialize = null;
        if ("kyro".equalsIgnoreCase(serialize)) {
            rpcSerialize = ReflectUtils.newInstance("com.kongzhong.mrpc.serialize.KyroSerialize", RpcSerialize.class);
        }
        if ("protostuff".equalsIgnoreCase(serialize)) {
            rpcSerialize = ReflectUtils.newInstance("com.kongzhong.mrpc.serialize.ProtostuffSerialize", RpcSerialize.class);
        }

        LbStrategyEnum lbStrategyEnum = LbStrategyEnum.valueOf(this.lbStrategy.toUpperCase());
        HaStrategyEnum haStrategyEnum = HaStrategyEnum.valueOf(this.haStrategy.toUpperCase());

        ClientConfig.me().setAppId(appId);
        ClientConfig.me().setRpcSerialize(rpcSerialize);
        ClientConfig.me().setHaStrategy(haStrategyEnum);
        ClientConfig.me().setLbStrategy(lbStrategyEnum);
        ClientConfig.me().setSkipBind(skipBind);
        ClientConfig.me().setRetryInterval(retryInterval);
        ClientConfig.me().setRetryCount(retryCount);
        ClientConfig.me().setWaitTimeout(waitTimeout);
        ClientConfig.me().setPingInterval(pingInterval);

        log.info("{}", ClientConfig.me());

        isInit = true;
    }

    /**
     * 同步直连
     */
    protected void directConnect() {
        Map<String, Set<String>> mappings = Maps.newHashMap();
        directAddressList.forEach((directAddress, clientBeans) -> {
            Set<String> serviceNames = clientBeans.stream().map(ClientBean::getServiceName).collect(Collectors.toSet());
            mappings.put(directAddress, serviceNames);
        });
        if (null != nettyConfig) {
            Connections.me().setNettyConfig(nettyConfig);
        }
        Connections.me().syncConnect(mappings);
    }

    /**
     * 设置默认注册中心
     *
     * @param serviceDiscovery 注册中心
     */
    public void setDefaultDiscovery(ServiceDiscovery serviceDiscovery) {
        serviceDiscoveryMap.put("default", serviceDiscovery);
    }

    /**
     * 添加一个客户端拦截器
     *
     * @param inteceptor 客户端拦截器
     */
    public void addInterceptor(RpcClientInterceptor inteceptor) {
        if (null != inteceptor) {
            log.info("Add interceptor [{}]", inteceptor.toString());
            this.rpcClientInterceptors.add(inteceptor);
        }
    }

    /**
     * 初始化客户端引用
     *
     * @param clientBean  客户端引用Bean
     * @param beanFactory Bean工厂
     */
    protected void initReferer(ClientBean clientBean, ConfigurableListableBeanFactory beanFactory) {
        String   serviceName  = clientBean.getServiceName();
        Class<?> serviceClass = clientBean.getServiceClass();
        try {
            if (DEFAULT_CLIENT_TIMEOUT.equals(clientBean.getWaitTimeout())) {
                clientBean.setWaitTimeout(ClientConfig.me().getWaitTimeout());
            }
            Object object = this.getProxyBean(clientBean.getWaitTimeout(), serviceClass);
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
                if (StringUtils.isEmpty(clientBean.getAppId())) {
                    clientBean.setAppId(ClientConfig.me().getAppId());
                }
                ClientConfig.me().getServiceDiscoveryMap().put(serviceName, serviceDiscovery);
                serviceDiscovery.discover(clientBean);
            } else {
                String directAddress = this.getDirectAddress(clientBean);
                if (StringUtils.isEmpty(directAddress)) {
                    throw new SystemException("Service discovery or direct address must select one.");
                }

                log.debug("Service [{}] direct to [{}]", serviceName, directAddress);

                Stream.of(directAddress.split(",")).forEach(address -> {
                    List<ClientBean> directUrlServices = directAddressList.getOrDefault(address, new ArrayList<>());
                    directUrlServices.add(clientBean);
                    directAddressList.put(address, directUrlServices);
                });
            }
            log.info("Bind rpc service [{}]", serviceName);
        } catch (Exception e) {
            log.error("Bind rpc service [{}] error", serviceName, e);
        }
    }

    /**
     * 返回引用是否使用注册中心
     *
     * @param clientBean 客户端引用Bean
     * @return 返回客户端引用是否使用注册中心
     */
    protected boolean usedRegistry(ClientBean clientBean) {
        return StringUtils.isNotEmpty(clientBean.getRegistry()) || serviceDiscoveryMap.containsKey("default") && StringUtils.isEmpty(clientBean.getDirectAddress());
    }

    /**
     * 返回客户端的直连地址
     *
     * @param clientBean 客户端引用Bean
     * @return 返回客户端直连服务端地址
     */
    protected String getDirectAddress(ClientBean clientBean) {
        String directAddress = StringUtils.isNotEmpty(clientBean.getDirectAddress()) ? clientBean.getDirectAddress() : this.directAddress;
        clientBean.setDirectAddress(directAddress);
        return directAddress;
    }

    ServiceDiscovery parseRegistry(RegistryBean registryBean) {
        String type = registryBean.getType();
        if (RegistryEnum.DEFAULT.getName().equals(type)) {
            return new DefaultDiscovery();
        }
        try {
            if (RegistryEnum.ZOOKEEPER.getName().equals(type)) {
                String zkAddress          = registryBean.getAddress();
                Object zookeeperDiscovery = Class.forName("com.kongzhong.mrpc.discover.ZookeeperServiceDiscovery").getConstructor(String.class).newInstance(zkAddress);
                return (ServiceDiscovery) zookeeperDiscovery;
            }
        } catch (Exception e) {
            throw new SystemException("Parse ServiceDiscovery error", e);
        }
        return null;
    }

    public static <T> T getBean(Class<T> type) {
        return beanFactory.getBean(type);
    }

    public static Object getBean(String beanName) {
        return beanFactory.getBean(beanName);
    }

    protected void startFinish() {
        if (adminConfig.isEnabled()) {
            this.sendClientStatus(NodeStatusEnum.ONLINE);
        }
    }

    /**
     * 发送服务状态给后台
     */
    private void sendClientStatus(NodeStatusEnum nodeStatus) {
        String url = adminConfig.getUrl() + "/api/client";
        log.info("发送: {}", url);

        RpcClientNotice rpcClientNotice = new RpcClientNotice();
        rpcClientNotice.setAppId(System.getProperty("APPID", this.appId));
        if (nodeStatus == NodeStatusEnum.ONLINE) {
            rpcClientNotice.setOnlineTime(LocalDateTime.now().format(COMMON_DATE_TIME_FORMATTER));
        }
        if (nodeStatus == NodeStatusEnum.OFFLINE) {
            rpcClientNotice.setOfflineTime(LocalDateTime.now().format(COMMON_DATE_TIME_FORMATTER));
        }
        rpcClientNotice.setHost(NetUtils.getSiteIp());
        rpcClientNotice.setPid(NetUtils.getPID());

        Set<String> services = clientBeans.stream().map(ClientBean::getServiceName).collect(Collectors.toSet());
        rpcClientNotice.setServices(services);

        try {
            String body = JacksonSerialize.toJSONString(rpcClientNotice);
            int code = HttpRequest.post(url)
                    .contentType("application/json;charset=utf-8")
                    .connectTimeout(10_000)
                    .readTimeout(5000)
                    .header("notice_status", nodeStatus.toString())
                    .header("address", NetUtils.getSiteIp())
                    .basic(adminConfig.getUsername(), adminConfig.getPassword())
                    .send(body).code();

            log.debug("Response code: {}", code);
        } catch (HttpRequest.HttpRequestException e) {
            log.debug("连接失败");
        } catch (Exception e) {
            log.error("Send error", e);
        }
    }

    /**
     * 停止客户端，释放资源
     */
    public void shutdown() {
        this.close();
    }

    /**
     * 停止客户端，释放资源
     */
    public void close() {
        try {
            lock.lock();
            if (isClosed) {
                return;
            }
            log.info("UnRegistering mrpc client on shutdown");
            HttpClientHandler.shutdown();
            Connections.me().shutdown();
        } finally {
            isClosed = true;
            lock.unlock();
        }
    }

}
package com.kongzhong.mrpc.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.*;
import com.kongzhong.mrpc.Const;
import com.kongzhong.mrpc.common.thread.RpcThreadPool;
import com.kongzhong.mrpc.config.AdminConfig;
import com.kongzhong.mrpc.config.NettyConfig;
import com.kongzhong.mrpc.config.ServerConfig;
import com.kongzhong.mrpc.embedded.ConfigService;
import com.kongzhong.mrpc.embedded.ConfigServiceImpl;
import com.kongzhong.mrpc.enums.EventType;
import com.kongzhong.mrpc.enums.NodeAliveStateEnum;
import com.kongzhong.mrpc.enums.RegistryEnum;
import com.kongzhong.mrpc.event.Event;
import com.kongzhong.mrpc.event.EventManager;
import com.kongzhong.mrpc.exception.InitializeException;
import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.exception.SystemException;
import com.kongzhong.mrpc.model.*;
import com.kongzhong.mrpc.registry.DefaultRegistry;
import com.kongzhong.mrpc.registry.ServiceRegistry;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import com.kongzhong.mrpc.serialize.jackson.JacksonSerialize;
import com.kongzhong.mrpc.transport.TransferSelector;
import com.kongzhong.mrpc.transport.netty.SimpleServerHandler;
import com.kongzhong.mrpc.utils.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.kongzhong.mrpc.Const.HEADER_REQUEST_ID;

/**
 * 抽象服务端请求处理器
 *
 * @author biezhi
 * 2017/4/19
 */
@Slf4j
@NoArgsConstructor
@ToString(exclude = {"rpcMapping", "transferSelector"})
public abstract class SimpleRpcServer {

    public static String LOCAL_ADDRESS = "";

    public static Boolean PRINT_ERROR_LOG = false;

    /**
     * RPC服务映射
     */
    protected RpcMapping rpcMapping = RpcMapping.me();

    /**
     * 是否使用了注册中心
     */
    protected boolean usedRegistry;

    /**
     * 注册中心列表 [注册中心名->注册中心实现]
     */
    protected Map<String, ServiceRegistry> serviceRegistryMap = Maps.newHashMap();

    /**
     * 服务端拦截器，多个用逗号相隔，顺序拦截
     */
    @Getter
    @Setter
    protected String interceptors;

    /**
     * 服务器权重，当用到加权轮训负载均衡策略时有用
     */
    @Setter
    protected int weight;

    /**
     * rpc服务地址
     */
    @Getter
    @Setter
    protected String address;

    /**
     * 弹性ip地址，不清楚可不填
     */
    @Getter
    @Setter
    protected String elasticIp;

    /**
     * 业务线程池前缀
     */
    @Getter
    @Setter
    protected String poolName = "mrpc-server";

    /**
     * 序列化类型，默认protostuff
     */
    @Getter
    @Setter
    protected String serialize;

    /**
     * appId
     */
    @Getter
    @Setter
    protected String appId;

    @Getter
    @Setter
    protected String appName;

    @Getter
    @Setter
    protected String owner;

    /**
     * 是否是测试环境，如果 "true" 则在启动后不会挂起程序
     */
    @Getter
    @Setter
    protected String test;

    /**
     * 传输协议选择器
     */
    private TransferSelector transferSelector;

    /**
     * netty服务端配置
     */
    @Getter
    @Setter
    protected NettyConfig nettyConfig;

    /**
     * 后台配置
     */
    @Getter
    @Setter
    protected AdminConfig adminConfig;

    private static final Map<String, String> SERVER_CONTEXT = Maps.newHashMap();

    /**
     * 服务端处理线程池
     */
    private static ListeningExecutorService LISTENING_EXECUTOR_SERVICE;

    private static List<ListenableFuture> listenableFutures = Lists.newCopyOnWriteArrayList();

    private ScheduledFuture adminSchedule;

    private volatile boolean isClosed = false;

    private Lock lock = new ReentrantLock();

    /**
     * 启动RPC服务端
     */
    protected void startServer() {
        this.initConfig();
        this.bindRpcServer();
    }

    private void initConfig() {
        if (null == nettyConfig) {
            nettyConfig = new NettyConfig(128, true);
        }
        if (null == serialize) {
            serialize = "kyro";
        }

        if (CollectionUtils.isNotEmpty(serviceRegistryMap)) {
            usedRegistry = true;
        }

        RpcSerialize rpcSerialize = null;
        if ("kyro".equalsIgnoreCase(serialize)) {
            rpcSerialize = ReflectUtils.newInstance("com.kongzhong.mrpc.serialize.KyroSerialize", RpcSerialize.class);
        }
        if ("protostuff".equalsIgnoreCase(serialize)) {
            rpcSerialize = ReflectUtils.newInstance("com.kongzhong.mrpc.serialize.ProtostuffSerialize", RpcSerialize.class);
        }

        if (null == rpcSerialize) {
            throw new InitializeException("RPC server serialize is null.");
        }

        transferSelector = new TransferSelector(rpcSerialize);
        int businessThreadPoolSize = nettyConfig.getBusinessThreadPoolSize();
        setListeningExecutorService(businessThreadPoolSize);
    }

    public static void setListeningExecutorService(int businessThreadPoolSize) {
        LISTENING_EXECUTOR_SERVICE = MoreExecutors.listeningDecorator((ThreadPoolExecutor) RpcThreadPool.getExecutor(businessThreadPoolSize, -1));
    }

    private void bindRpcServer() {

        // 服务启动时
        EventManager.me().fireEvent(EventType.SERVER_STARTING, Event.builder().rpcContext(RpcContext.get()).build());

//        ThreadFactory threadRpcFactory = new NamedThreadFactory(poolName);
//        int parallel = Runtime.getRuntime().availableProcessors() * 2;

        EventLoopGroup boss   = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker).channel(NioServerSocketChannel.class)
                    .childHandler(transferSelector.getServerChannelHandler())
                    .option(ChannelOption.SO_BACKLOG, nettyConfig.getBacklog())
                    .childOption(ChannelOption.SO_KEEPALIVE, nettyConfig.isKeepalive())
                    .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(nettyConfig.getLowWaterMark(), nettyConfig.getHighWaterMark()));

            String[] ipAddress = address.split(":");
            String   host      = null;
            int      port      = -1;

            if (ipAddress.length == 1) {
                host = NetUtils.getLocalAddress().getHostAddress();
                port = Integer.parseInt(ipAddress[0]);
                this.address = host + ':' + port;
            }

            if (ipAddress.length == 2) {
                host = ipAddress[0];
                port = Integer.parseInt(ipAddress[1]);
            }

            //获取服务器IP地址和端口
            ServerConfig.me().setElasticIp(elasticIp);
            ChannelFuture future = bootstrap.bind(host, port).sync();

            this.registerEmbedded();

            //注册服务
            rpcMapping.getServiceBeanMap().values().forEach(serviceBean -> {

                String  appId        = this.getAppId(serviceBean);
                String  address      = this.getBindAddress(serviceBean);
                String  elasticIp    = this.getRegisterElasticIp(serviceBean);
                boolean usedRegistry = this.usedRegistry(serviceBean);
                String  serviceName  = serviceBean.getServiceName();

                if (usedRegistry) {
                    // 查找该服务的注册中心
                    ServiceRegistry serviceRegistry = this.getRegistry(serviceBean);
                    try {
                        serviceBean.setRegistry(this.getRegistryName(serviceBean));
                        serviceBean.setAppId(appId);
                        serviceBean.setAddress(address);
                        serviceBean.setElasticIp(elasticIp);
                        serviceRegistry.register(serviceBean);

                        ServiceStatusTable.me().createServiceStatus(serviceBean, weight);
                    } catch (RpcException e) {
                        log.error("Service register error", e);
                    }
                }
                if (StringUtils.isNotEmpty(elasticIp)) {
                    log.info("Register => [{}] - [{}]/[{}]", serviceName, address, elasticIp);
                } else {
                    log.info("Register => [{}] - [{}]", serviceName, address);
                }
                // 服务注册后
                EventManager.me().fireEvent(EventType.SERVER_SERVICE_REGISTER, Event.builder().rpcContext(RpcContext.get()).build());
            });

            log.info("Publish services finished, mrpc version [{}]", Const.VERSION);

            LOCAL_ADDRESS = this.address;

            // 服务启动后
            EventManager.me().fireEvent(EventType.SERVER_STARTED, Event.builder().rpcContext(RpcContext.get()).build());

            Runtime.getRuntime().addShutdownHook(new Thread(this::close));

            this.channelSync(future);

        } catch (Exception e) {
            log.error("RPC server start error", e);
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }

    /**
     * 注册内置服务
     */
    private void registerEmbedded() {
        // 注册内置服务
        ConfigService configService     = ConfigServiceImpl.me();
        ServiceBean   configServiceBean = new ServiceBean();
        configServiceBean.setBean(configService);
        configServiceBean.setServiceName(ConfigService.class.getName());
        configServiceBean.setAppId(appId);
        configServiceBean.setAddress(address);
        configServiceBean.setElasticIp(elasticIp);
        rpcMapping.addServiceBean(configServiceBean);
    }

    /**
     * 后台监听
     */
    private void channelSync(ChannelFuture future) throws InterruptedException {

        if (null != adminConfig && adminConfig.isEnabled()) {

            // 有客户端连接
            EventManager.me().addEventListener(EventType.SERVER_CLIENT_CONNECTED, e -> ServiceStatusTable.me().addClient());

            // 有客户端断开
            EventManager.me().addEventListener(EventType.SERVER_CLIENT_DISCONNECT, e -> ServiceStatusTable.me().removeClient());

            // 停止服务
            EventManager.me().addEventListener(EventType.SHUTDOWN_SERVER, e -> {
                cancelAdminSchedule();
                sendServerStatus(NodeAliveStateEnum.DEAD);
            });

            adminSchedule = future.channel().eventLoop().scheduleAtFixedRate(() -> sendServerStatus(NodeAliveStateEnum.ALIVE), 500, adminConfig.getPeriod(), TimeUnit.MILLISECONDS);
        }

        if ("true".equals(this.test)) {
            new Thread(() -> {
                try {
                    future.channel().closeFuture().sync();
                } catch (Exception e) {
                    log.error("", e);
                }
            }).start();
        } else {
            future.channel().closeFuture().sync();
        }
    }

    /**
     * 发送服务状态给后台
     */
    private void sendServerStatus(NodeAliveStateEnum aliveState) {
        String url = adminConfig.getUrl() + "/api/service";
        ServiceNodePayload serviceNodePayload = ServiceNodePayload.builder()
                .address(this.address)
                .appId(this.appId)
                .aliveState(aliveState)
                .services(ServiceStatusTable.me().getServiceStatus())
                .build();

        try {
            String body = JacksonSerialize.toJSONString(serviceNodePayload);
            log.debug("Request URL\t: {}", url);
            log.debug("Send body\t\t: {}", body);

            int code = HttpRequest.post(url)
                    .contentType("application/json;charset=utf-8")
                    .connectTimeout(10_000)
                    .readTimeout(5000)
                    .basic(adminConfig.getUsername(), adminConfig.getPassword())
                    .send(body).code();

            log.debug("Response code: {}", code);
        } catch (HttpRequest.HttpRequestException e) {
            log.debug("连接失败");
        } catch (Exception e) {
            log.error("Send error", e);
            cancelAdminSchedule();
        }
    }

    private void cancelAdminSchedule() {
        adminSchedule.cancel(true);
    }

    /**
     * 返回引用是否使用注册中心
     *
     * @param serviceBean 服务Bean
     * @return 返回该服务是否使用了注册中心
     */
    private boolean usedRegistry(ServiceBean serviceBean) {
        return StringUtils.isNotEmpty(serviceBean.getRegistry()) || serviceRegistryMap.containsKey("default");
    }

    protected String getAppId(ServiceBean serviceBean) {
        String appId = this.appId;
        if (StringUtils.isNotEmpty(serviceBean.getAppId())) {
            appId = serviceBean.getAppId();
        }
        return appId;
    }

    /**
     * 获取服务暴露的地址 ip:port
     *
     * @param serviceBean 服务Bean
     * @return 返回该服务绑定的地址
     */
    protected String getBindAddress(ServiceBean serviceBean) {
        String address = this.address;
        if (StringUtils.isNotEmpty(serviceBean.getAddress())) {
            address = serviceBean.getAddress();
        }
        return address;
    }

    protected String getRegisterElasticIp(ServiceBean serviceBean) {
        String elasticIp = this.elasticIp;
        if (StringUtils.isNotEmpty(serviceBean.getElasticIp())) {
            elasticIp = serviceBean.getElasticIp();
        }
        return elasticIp;
    }

    /**
     * 获取服务使用的注册中心
     *
     * @param serviceBean 服务Bean
     * @return 返回当前服务的注册中心
     */
    protected ServiceRegistry getRegistry(ServiceBean serviceBean) {
        return serviceRegistryMap.get(getRegistryName(serviceBean));
    }

    private String getRegistryName(ServiceBean serviceBean) {
        return StringUtils.isNotEmpty(serviceBean.getRegistry()) ? serviceBean.getRegistry() : "default";
    }

    /**
     * 提交任务,异步获取结果.
     *
     * @param task     任务
     * @param ctx      Netty上下文
     * @param request  RpcRequest请求对象
     * @param response RpcResponse请求对象
     */
    public static void submit(Callable<Boolean> task, final ChannelHandlerContext ctx, final RpcRequest request, final RpcResponse response) {

        //提交任务, 异步获取结果
        ListenableFuture<Boolean> listenableFuture = LISTENING_EXECUTOR_SERVICE.submit(task);

        //注册回调函数, 在task执行完之后 异步调用回调函数
        Futures.addCallback(listenableFuture, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                //为返回msg回客户端添加一个监听器,当消息成功发送回客户端时被异步调用.
                // 服务端回显 request已经处理完毕
                String requestId = request.getRequestId();
                ctx.writeAndFlush(response).addListener((ChannelFutureListener) channelFuture -> {
                    log.debug("Server execute [{}] success.", requestId);
                    listenableFutures.remove(listenableFuture);
                });
            }

            @Override
            public void onFailure(Throwable t) {
//                log.error("", t);
            }
        }, LISTENING_EXECUTOR_SERVICE);
        listenableFutures.add(listenableFuture);
    }

    public static void submit(Callable<FullHttpResponse> task, final ChannelHandlerContext ctx) {
        //提交任务, 异步获取结果
        ListenableFuture<FullHttpResponse> listenableFuture = LISTENING_EXECUTOR_SERVICE.submit(task);
        //注册回调函数, 在task执行完之后 异步调用回调函数
        Futures.addCallback(listenableFuture, new FutureCallback<FullHttpResponse>() {
            @Override
            public void onSuccess(FullHttpResponse response) {
                // 服务端响应前
                EventManager.me().fireEvent(EventType.SERVER_PRE_RESPONSE, Event.builder().rpcContext(RpcContext.get()).build());
                //为返回msg回客户端添加一个监听器,当消息成功发送回客户端时被异步调用.
                String requestId = response.headers().get(HEADER_REQUEST_ID);

                // 判断客户端是否存活，如果不存活则不发送
                if (ctx.channel().isActive()) {
                    ctx.writeAndFlush(response).addListener((ChannelFutureListener) channelFuture -> {
                        if (channelFuture.isSuccess()) {
                            log.debug("Server send to {} success, requestId [{}]", ctx.channel(), requestId);
                        } else {
                            log.debug("Server send to {} fail, requestId [{}]", ctx.channel(), requestId);
                        }
                        listenableFutures.remove(listenableFuture);
                    });
                } else {
                    log.warn("Channel Inactived: {}", ctx.channel());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                log.error("", t);
            }
        }, LISTENING_EXECUTOR_SERVICE);
        listenableFutures.add(listenableFuture);
    }

    /**
     * 将map转换为注册中心实现
     *
     * @param map 将Map中的注册中心筛选出来
     * @return 返回筛选出来的注册中心
     */

    protected ServiceRegistry mapToRegistry(Map<String, String> map) {
        String type = map.get("type");
        if (RegistryEnum.DEFAULT.getName().equals(type)) {
            return new DefaultRegistry();
        }
        // Zookeeper注册中心
        if (RegistryEnum.ZOOKEEPER.getName().equals(type)) {
            String zkAddress = map.get("address");
            if (StringUtils.isEmpty(zkAddress)) {
                throw new SystemException("Zookeeper connect address not is empty");
            }
            log.info("RPC server connect zookeeper address: {}", zkAddress);
            return this.getZookeeperServiceRegistry(zkAddress);
        }
        return null;
    }

    ServiceRegistry getZookeeperServiceRegistry(String zkAddress) {
        try {
            Object zookeeperServiceRegistry = Class.forName("com.kongzhong.mrpc.registry.ZookeeperServiceRegistry").getConstructor(String.class).newInstance(zkAddress);
            return (ServiceRegistry) zookeeperServiceRegistry;
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    protected static void setContext(String key, String value) {
        SERVER_CONTEXT.put(key, value);
    }

    public static String getContext(String key) {
        return SERVER_CONTEXT.get(key);
    }

    /**
     * 销毁资源,卸载服务
     */
    public void close() {
        lock.lock();
        try {
            if (isClosed) {
                return;
            }
            log.info("UnRegistering mrpc server on shutdown");
            isClosed = true;

            // 拒绝连接
            SimpleServerHandler.shutdown();
            EventManager.me().fireEvent(EventType.SHUTDOWN_SERVER, Event.builder().rpcContext(RpcContext.get()).build());

            for (ListenableFuture listenableFuture : listenableFutures) {
                while (!listenableFuture.isDone()) {
                    TimeUtils.sleep(100);
                }
            }

            LISTENING_EXECUTOR_SERVICE.shutdown();
            rpcMapping.getServiceBeanMap().values().forEach(serviceBean -> {
                String          serviceName     = serviceBean.getServiceName();
                ServiceRegistry serviceRegistry = getRegistry(serviceBean);
                try {
                    if (null != serviceRegistry) {
                        serviceRegistry.unRegister(serviceBean);
                        log.debug("UnRegister service => [{}]", serviceName);
                    }
                } catch (Exception e) {
                    log.error("UnRegister service error", e);
                }
            });
        } finally {
            lock.unlock();
        }
    }
}
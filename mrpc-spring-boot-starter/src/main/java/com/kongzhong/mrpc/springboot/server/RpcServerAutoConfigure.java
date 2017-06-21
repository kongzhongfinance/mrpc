package com.kongzhong.mrpc.springboot.server;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.*;
import com.kongzhong.mrpc.common.thread.NamedThreadFactory;
import com.kongzhong.mrpc.common.thread.RpcThreadPool;
import com.kongzhong.mrpc.config.DefaultConfig;
import com.kongzhong.mrpc.config.NettyConfig;
import com.kongzhong.mrpc.config.ServerConfig;
import com.kongzhong.mrpc.enums.RegistryEnum;
import com.kongzhong.mrpc.interceptor.RpcServerInteceptor;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.model.ServiceBean;
import com.kongzhong.mrpc.registry.DefaultRegistry;
import com.kongzhong.mrpc.registry.ServiceRegistry;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import com.kongzhong.mrpc.server.RpcMapping;
import com.kongzhong.mrpc.springboot.config.CommonProperties;
import com.kongzhong.mrpc.springboot.config.RpcServerProperties;
import com.kongzhong.mrpc.transport.TransferSelector;
import com.kongzhong.mrpc.utils.StringUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.nio.channels.spi.SelectorProvider;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import static com.kongzhong.mrpc.Const.HEADER_REQUEST_ID;

@EnableConfigurationProperties({CommonProperties.class, RpcServerProperties.class})
@ConditionalOnProperty("mrpc.server.transport")
@Slf4j
public class RpcServerAutoConfigure {

    @Autowired
    private CommonProperties commonProperties;

    @Autowired
    private RpcServerProperties rpcServerProperties;

    @Autowired
    private ConfigurableBeanFactory configurableBeanFactory;

    private RpcMapping rpcMapping = RpcMapping.me();

    /**
     * 序列化类型，kyro
     */
    private RpcSerialize serialize;

    /**
     * 服务注册实例
     */
    private Map<String, ServiceRegistry> serviceRegistryMap = Maps.newHashMap();

    /**
     * 自定义服务配置
     */
    private Map<String, Map<String, String>> customServiceMap = Maps.newHashMap();

    /**
     * 是否使用注册中心
     */
    private boolean usedRegistry;

    /**
     * 传输协议选择
     */
    private TransferSelector transferSelector;

    /**
     * 拦截器列表, 默认添加性能监控拦截器
     */
    private List<RpcServerInteceptor> interceptorList;

    /**
     * netty服务端配置
     */
    private NettyConfig nettyConfig;

    private static final ListeningExecutorService LISTENING_EXECUTOR_SERVICE = MoreExecutors.listeningDecorator((ThreadPoolExecutor) RpcThreadPool.getExecutor(16, -1));

    @Bean
    public InitBean initBean() {
        log.debug("Initializing rpc server bean");
        return new InitBean(rpcMapping);
    }

    @Bean
    @ConditionalOnBean(InitBean.class)
    public BeanFactoryAware beanFactoryAware() {
        return (beanFactory) -> {
            log.debug("Initializing rpc server beanFactoryAware ");
            // 注册中心
            if (null != commonProperties.getRegistry() && !commonProperties.getRegistry().isEmpty()) {
                commonProperties.getRegistry().forEach((registryName, map) -> {
                    ServiceRegistry serviceRegistry = mapToRegistry(map);
                    serviceRegistryMap.put(registryName, serviceRegistry);
                    configurableBeanFactory.registerSingleton(registryName, serviceRegistry);
                    usedRegistry = true;
                });
            }
            if (null != commonProperties.getCustom()) {
                customServiceMap = commonProperties.getCustom();
            }
            RpcServerAutoConfigure.this.startServer();
        };
    }

    private ServiceRegistry mapToRegistry(Map<String, String> map) {
        String type = map.get("type");
        if (RegistryEnum.DEFAULT.getName().equals(type)) {
            ServiceRegistry serviceRegistry = new DefaultRegistry();
            return serviceRegistry;
        }
        if (RegistryEnum.ZOOKEEPER.getName().equals(type)) {
            String zkAddr = map.getOrDefault("address", "127.0.0.1:2181");
            log.info("RPC server connect zookeeper address: {}", zkAddr);
            try {
                Object zookeeperServiceRegistry = Class.forName("com.kongzhong.mrpc.registry.ZookeeperServiceRegistry").getConstructor(String.class).newInstance(zkAddr);
                ServiceRegistry serviceRegistry = (ServiceRegistry) zookeeperServiceRegistry;
                return serviceRegistry;
            } catch (Exception e) {
                log.error("", e);
            }
        }
        return null;
    }

    /**
     * 提交任务,异步获取结果.
     *
     * @param task
     * @param ctx
     * @param request
     * @param response
     */
    public static void submit(Callable<Boolean> task, final ChannelHandlerContext ctx, final RpcRequest request, final RpcResponse response) {

        //提交任务, 异步获取结果
        ListenableFuture<Boolean> listenableFuture = LISTENING_EXECUTOR_SERVICE.submit(task);

        //注册回调函数, 在task执行完之后 异步调用回调函数
        Futures.addCallback(listenableFuture, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                //为返回msg回客户端添加一个监听器,当消息成功发送回客户端时被异步调用.
                ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
                    /**
                     * 服务端回显 request已经处理完毕
                     * @param channelFuture
                     * @throws Exception
                     */
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        log.debug("Request id [{}] success.", request.getRequestId());
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                log.error("", t);
            }
        }, LISTENING_EXECUTOR_SERVICE);
    }

    public static void submit(Callable<FullHttpResponse> task, final ChannelHandlerContext ctx) {
        //提交任务, 异步获取结果
        ListenableFuture<FullHttpResponse> listenableFuture = LISTENING_EXECUTOR_SERVICE.submit(task);
        //注册回调函数, 在task执行完之后 异步调用回调函数
        Futures.addCallback(listenableFuture, new FutureCallback<FullHttpResponse>() {
            @Override
            public void onSuccess(FullHttpResponse response) {
                //为返回msg回客户端添加一个监听器,当消息成功发送回客户端时被异步调用.
                ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
                    /**
                     * 服务端回显 request已经处理完毕
                     * @param channelFuture
                     * @throws Exception
                     */
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        log.debug("Request id [{}] success.", response.headers().get(HEADER_REQUEST_ID));
                    }

                });
            }

            @Override
            public void onFailure(Throwable t) {
                log.error("", t);
            }
        }, LISTENING_EXECUTOR_SERVICE);
    }

    protected void startServer() {
        String transport = rpcServerProperties.getTransport();

        if (null == nettyConfig) {
            nettyConfig = DefaultConfig.nettyServerConfig();
        }

        if (null == serialize) {
            serialize = DefaultConfig.serialize();
        }

        transferSelector = new TransferSelector(serialize);

        ThreadFactory threadRpcFactory = new NamedThreadFactory(rpcServerProperties.getPoolName());
        int parallel = Runtime.getRuntime().availableProcessors() * 2;

        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup(parallel, threadRpcFactory, SelectorProvider.provider());

        try {

            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker).channel(NioServerSocketChannel.class)
                    .childHandler(transferSelector.getServerChannelHandler(transport))
                    .option(ChannelOption.SO_BACKLOG, nettyConfig.getBacklog())
                    .childOption(ChannelOption.SO_KEEPALIVE, nettyConfig.isKeepalive())
                    .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(nettyConfig.getLowWaterMark(), nettyConfig.getHighWaterMark()));

            String[] ipAddr = rpcServerProperties.getAddress().split(":");
            if (ipAddr.length == 2) {
                //获取服务器IP地址和端口
                String host = ipAddr[0];
                int port = Integer.parseInt(ipAddr[1]);

                ServerConfig.me().setAddress(rpcServerProperties.getAddress());
                ServerConfig.me().setElasticIp(rpcServerProperties.getElasticIp());

                if (null != rpcServerProperties.getAppId()) {
                    ServerConfig.me().setAppId(rpcServerProperties.getAppId());
                }

                ChannelFuture future = bootstrap.bind(host, port).sync();

                //注册服务
                rpcMapping.getServiceBeanMap().values().forEach(serviceBean -> {
                    String serviceName = serviceBean.getServiceName();
                    String address = this.getAddress(serviceBean);
                    if (usedRegistry) {
                        // 查找该服务的注册中心
                        ServiceRegistry serviceRegistry = this.getRegistry(serviceBean);
                        serviceRegistry.register(serviceName);
                    }
                    log.info("Register => [{}] - [{}]", serviceName, address);
                });

                log.info("Publish services finished!");
//                log.info("RPC server start with => {}", port);

                if (usedRegistry) {
                    this.listenDestroy();
                }

                if ("true".equals(commonProperties.getTest())) {
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
            } else {
                log.warn("RPC server start fail.");
            }
        } catch (Exception e) {
            log.error("RPC server start error", e);
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }

    /**
     * 获取服务暴露的地址 ip:port
     *
     * @param serviceBean
     * @return
     */
    private String getAddress(ServiceBean serviceBean) {
        String address = rpcServerProperties.getAddress();
        Map<String, String> custom = customServiceMap.get(serviceBean.getServiceName());
        if (null != custom && custom.containsKey("address")) {
            address = custom.get("address");
        }
        address = StringUtils.isNotEmpty(serviceBean.getAddress()) ? serviceBean.getAddress() : address;
        return address;
    }

    /**
     * 获取服务使用的注册中心
     *
     * @param serviceBean
     * @return
     */
    private ServiceRegistry getRegistry(ServiceBean serviceBean) {
        String registryName = null;
        Map<String, String> custom = customServiceMap.get(serviceBean.getServiceName());
        if (null != custom && custom.containsKey("registry")) {
            registryName = custom.get("registry");
        }
        registryName = StringUtils.isNotEmpty(serviceBean.getRegistry()) ? serviceBean.getRegistry() : registryName;
        return serviceRegistryMap.get(registryName);
    }

    /**
     * 销毁资源
     */
    protected void listenDestroy() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            rpcMapping.getServiceBeanMap().values().forEach(serviceBean -> {
                String serviceName = serviceBean.getServiceName();
                ServiceRegistry serviceRegistry = getRegistry(serviceBean);
                serviceRegistry.unregister(serviceName);
                log.debug("Unregister => [{}]", serviceName);
            });
        }));
    }

}
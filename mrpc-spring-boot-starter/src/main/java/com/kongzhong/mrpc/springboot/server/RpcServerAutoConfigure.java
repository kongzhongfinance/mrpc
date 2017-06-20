package com.kongzhong.mrpc.springboot.server;

import com.google.common.util.concurrent.*;
import com.kongzhong.mrpc.common.thread.NamedThreadFactory;
import com.kongzhong.mrpc.common.thread.RpcThreadPool;
import com.kongzhong.mrpc.config.DefaultConfig;
import com.kongzhong.mrpc.config.NettyConfig;
import com.kongzhong.mrpc.config.ServerConfig;
import com.kongzhong.mrpc.enums.RegistryEnum;
import com.kongzhong.mrpc.interceptor.RpcInteceptor;
import com.kongzhong.mrpc.Const;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.registry.ServiceRegistry;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import com.kongzhong.mrpc.server.RpcMapping;
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
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.nio.channels.spi.SelectorProvider;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import static com.kongzhong.mrpc.Const.HEADER_REQUEST_ID;

@Configuration
@EnableConfigurationProperties(RpcServerProperties.class)
@ConditionalOnProperty("mrpc.server.transport")
@Slf4j
public class RpcServerAutoConfigure {

    @Autowired
    private Environment environment;

    @Autowired
    private ConfigurableBeanFactory configurableBeanFactory;

    protected RpcMapping rpcMapping = RpcMapping.me();

    /**
     * 序列化类型，默认protostuff
     */
    protected RpcSerialize serialize;

    @Autowired
    private RpcServerProperties rpcServerProperties;

    /**
     * 服务注册实例
     */
    protected ServiceRegistry serviceRegistry;

    /**
     * 传输协议选择
     */
    protected TransferSelector transferSelector;

    /**
     * 拦截器列表, 默认添加性能监控拦截器
     */
    protected List<RpcInteceptor> interceptorList;

    private String isTestEnv;

    /**
     * netty服务端配置
     */
    protected NettyConfig nettyConfig;

    protected static final ListeningExecutorService TPE = MoreExecutors.listeningDecorator((ThreadPoolExecutor) RpcThreadPool.getExecutor(16, -1));

    @Bean
    public InitBean initBean() {
        return new InitBean(rpcMapping);
    }

    @Bean
    @ConditionalOnBean(InitBean.class)
    public BeanFactoryAware beanFactoryAware() {

        this.isTestEnv = environment.getProperty(Const.TEST_KEY, "false");
        return (beanFactory) -> {
            // 注册中心
            String registry = rpcServerProperties.getRegistry();
            if (RegistryEnum.ZOOKEEPER.getName().equals(registry)) {
                String zkAddr = environment.getProperty(Const.ZK_SERVER_ADDRESS, "127.0.0.1:2181");

                log.info("mrpc server connect zookeeper address: {}", zkAddr);
                try {
                    Object zookeeperServiceRegistry = Class.forName("com.kongzhong.mrpc.registry.ZookeeperServiceRegistry").getConstructor(String.class).newInstance(zkAddr);
                    ServiceRegistry serviceRegistry = (ServiceRegistry) zookeeperServiceRegistry;
                    RpcServerAutoConfigure.this.serviceRegistry = serviceRegistry;
                    configurableBeanFactory.registerSingleton(Const.REGSITRY_INTERFACE, serviceRegistry);
                } catch (Exception e) {
                    log.error("", e);
                }
            }
            RpcServerAutoConfigure.this.startServer();

        };
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
        ListenableFuture<Boolean> listenableFuture = TPE.submit(task);

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
                        log.debug("request [{}] success.", request.getRequestId());
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                log.error("", t);
            }
        }, TPE);
    }

    public static void submit(Callable<FullHttpResponse> task, final ChannelHandlerContext ctx) {
        //提交任务, 异步获取结果
        ListenableFuture<FullHttpResponse> listenableFuture = TPE.submit(task);
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
                        log.debug("request [{}] success.", response.headers().get(HEADER_REQUEST_ID));
                    }

                });
            }

            @Override
            public void onFailure(Throwable t) {
                log.error("", t);
            }
        }, TPE);
    }

    protected void startServer() {
        String transport = rpcServerProperties.getTransport();
        if (null == rpcServerProperties.getTransport()) {
            transport = DefaultConfig.transport();
        }

        if (null == nettyConfig) {
            nettyConfig = DefaultConfig.nettyServerConfig();
        }

        if (null == serialize) {
            serialize = DefaultConfig.serialize();
        }

        transferSelector = new TransferSelector(serialize);

        ThreadFactory threadRpcFactory = new NamedThreadFactory("mrpc-server");
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

                if (null == serviceRegistry) {
                    serviceRegistry = DefaultConfig.registry();
                }

                //注册服务
                for (String serviceName : rpcMapping.getServiceBeanMap().keySet()) {
                    serviceRegistry.register(serviceName);
                    log.info("=> [{}] - [{}]", serviceName, rpcServerProperties.getAddress());
                }

                log.info("publish services finished!");
                log.info("mrpc server start with => {}", port);

                this.listenDestroy();

                String isTestEnv = System.getProperty("mrpc.test");
                if (StringUtils.isNotEmpty(isTestEnv)) {
                    this.isTestEnv = isTestEnv;
                }

                if ("true".equals(this.isTestEnv)) {
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
                log.warn("mrpc server start fail.");
            }
        } catch (Exception e) {
            log.error("start rpc server error", e);
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }

    /**
     * 销毁资源
     */
    protected void listenDestroy() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (String serviceName : rpcMapping.getServiceBeanMap().keySet()) {
                serviceRegistry.unregister(serviceName);
                log.debug("unregister => [{}] - [{}]", serviceName, rpcServerProperties.getAddress());
            }
        }));
    }

}
package com.kongzhong.mrpc.server;

import com.google.common.util.concurrent.*;
import com.kongzhong.mrpc.annotation.RpcService;
import com.kongzhong.mrpc.common.thread.NamedThreadFactory;
import com.kongzhong.mrpc.common.thread.RpcThreadPool;
import com.kongzhong.mrpc.model.ServerConfig;
import com.kongzhong.mrpc.enums.SerializeEnum;
import com.kongzhong.mrpc.enums.TransportEnum;
import com.kongzhong.mrpc.model.NoInterface;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.registry.ServiceRegistry;
import com.kongzhong.mrpc.spring.utils.AopTargetUtils;
import com.kongzhong.mrpc.transport.TransferSelector;
import com.kongzhong.mrpc.transport.http.HttpResponse;
import com.kongzhong.mrpc.utils.StringUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.nio.channels.spi.SelectorProvider;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

@Data
@NoArgsConstructor
public class RpcServer implements ApplicationContextAware, InitializingBean {

    public static final Logger log = LoggerFactory.getLogger(RpcServer.class);

    /**
     * 存储服务映射
     */
    private Map<String, Object> handlerMap = new ConcurrentHashMap<>();

    /**
     * rpc服务地址
     */
    private String serverAddress;

    /**
     * 序列化类型，默认protostuff
     */
    private String serialize = SerializeEnum.PROTOSTUFF.name();

    /**
     * 传输协议，默认tcp协议
     */
    private String transport = TransportEnum.TCP.name();

    /**
     * 服务注册实例
     */
    private ServiceRegistry serviceRegistry;

    /**
     * 传输协议选择
     */
    private TransferSelector transferSelector;

    private static final ListeningExecutorService TPE = MoreExecutors.listeningDecorator((ThreadPoolExecutor) RpcThreadPool.getExecutor(16, -1));

    public RpcServer(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public RpcServer(String serverAddress, ServiceRegistry serviceRegistry) {
        this.serverAddress = serverAddress;
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * ① 设置上下文
     *
     * @param ctx
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(RpcService.class);
        try {
            if (null != serviceBeanMap && !serviceBeanMap.isEmpty()) {
                for (Object serviceBean : serviceBeanMap.values()) {
                    Object realBean = AopTargetUtils.getTarget(serviceBean);
                    RpcService rpcService = realBean.getClass().getAnnotation(RpcService.class);
                    String serviceName = rpcService.value().getName();
                    String version = rpcService.version();
                    String name = rpcService.name();

                    if (StringUtils.isNotEmpty(name)) {
                        serviceName = name;
                    } else {
                        if (NoInterface.class.getName().equals(serviceName)) {
                            Class<?>[] intes = realBean.getClass().getInterfaces();
                            if (null == intes || intes.length != 1) {
                                serviceName = realBean.getClass().getName();
                            } else {
                                serviceName = intes[0].getName();
                            }
                        }
                    }

                    if (StringUtils.isNotEmpty(version)) {
                        serviceName += "_" + version;
                    }

                    handlerMap.put(serviceName, realBean);
                }
            }
            transferSelector = new TransferSelector(handlerMap, serialize);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    /**
     * ② 后置操作
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        ThreadFactory threadRpcFactory = new NamedThreadFactory("mrpc-server");
        int parallel = Runtime.getRuntime().availableProcessors() * 2;

        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup(parallel, threadRpcFactory, SelectorProvider.provider());

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker).channel(NioServerSocketChannel.class)
                    .childHandler(transferSelector.getServerChannelHandler(transport))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            String[] ipAddr = serverAddress.split(":");

            if (ipAddr.length == 2) {
                //获取服务器IP地址和端口
                String host = ipAddr[0];
                int port = Integer.parseInt(ipAddr[1]);

                ServerConfig.me().setHost(host);
                ServerConfig.me().setPort(port);

                ChannelFuture future = bootstrap.bind(host, port).sync();

                //注册服务
                for (String serviceName : handlerMap.keySet()) {
                    if (serviceRegistry != null) {
                        serviceRegistry.register(serviceName);
                    } else {
                        log.info("=> [{}] - [{}]", serviceName, serverAddress);
                    }
                }

                log.info("publish services finished!");
                log.info("mrpc server start with => {}", port);
                future.channel().closeFuture().sync();
            } else {
                log.warn("mrpc server start fail.");
            }
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }
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

    public static void submit(Callable<HttpResponse> task, final ChannelHandlerContext ctx) {

        //提交任务, 异步获取结果
        ListenableFuture<HttpResponse> listenableFuture = TPE.submit(task);

        //注册回调函数, 在task执行完之后 异步调用回调函数
        Futures.addCallback(listenableFuture, new FutureCallback<HttpResponse>() {
            @Override
            public void onSuccess(HttpResponse response) {

                //为返回msg回客户端添加一个监听器,当消息成功发送回客户端时被异步调用.
                ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
                    /**
                     * 服务端回显 request已经处理完毕
                     * @param channelFuture
                     * @throws Exception
                     */
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        log.debug("request [{}] success.", response.getRequestId());
                    }

                });
            }

            @Override
            public void onFailure(Throwable t) {
                log.error("", t);
            }
        }, TPE);
    }


}
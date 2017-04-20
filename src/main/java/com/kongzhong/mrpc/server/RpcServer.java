package com.kongzhong.mrpc.server;

import com.google.common.util.concurrent.*;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.common.thread.NamedThreadFactory;
import com.kongzhong.mrpc.common.thread.RpcThreadPool;
import com.kongzhong.mrpc.common.StringUtil;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.serialize.ProtostuffSerialize;
import com.kongzhong.mrpc.spring.annotation.MRpcService;
import com.kongzhong.mrpc.spring.bean.NoInterface;
import com.kongzhong.mrpc.spring.utils.AopTargetUtils;
import com.kongzhong.mrpc.transfer.ServerChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
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

public class RpcServer implements ApplicationContextAware, InitializingBean {

    public static final Logger log = LoggerFactory.getLogger(RpcServer.class);

    private Map<String, Object> handlerMap = new ConcurrentHashMap<>();

    private String serverAddress;

    private static final ListeningExecutorService threadPoolExecutor = MoreExecutors.listeningDecorator((ThreadPoolExecutor) RpcThreadPool.getExecutor(16, -1));

    public RpcServer() {

    }

    public RpcServer(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    /**
     * ① 设置上下文
     *
     * @param ctx
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        log.debug("setApplicationContext");

        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(MRpcService.class);
        try {
            if (null != serviceBeanMap && !serviceBeanMap.isEmpty()) {
                for (Object serviceBean : serviceBeanMap.values()) {
                    Object realBean = AopTargetUtils.getTarget(serviceBean);
                    MRpcService mRpcService = realBean.getClass().getAnnotation(MRpcService.class);
                    String serviceName = mRpcService.value().getName();
                    String version = mRpcService.version();
                    if (NoInterface.class.getName().equals(serviceName)) {
                        Class<?>[] intes = realBean.getClass().getInterfaces();
                        if (null == intes || intes.length != 1) {
                            serviceName = realBean.getClass().getName();
                        } else {
                            serviceName = intes[0].getName();
                        }
                    }
                    if (StringUtil.isNotEmpty(version)) {
                        serviceName += "_" + version;
                    }
                    handlerMap.put(serviceName, realBean);
                }
            }
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
        log.debug("afterPropertiesSet");

        ThreadFactory threadRpcFactory = new NamedThreadFactory("mrpc-server");

        int parallel = Runtime.getRuntime().availableProcessors() * 2;

        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup(parallel, threadRpcFactory, SelectorProvider.provider());

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker).channel(NioServerSocketChannel.class)
                    .childHandler(new ServerChannelInitializer(handlerMap, new ProtostuffSerialize()))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            String[] ipAddr = serverAddress.split(":");

            if (ipAddr.length == 2) {
                //获取服务器IP地址和端口
                String host = ipAddr[0];
                int port = Integer.parseInt(ipAddr[1]);

                ChannelFuture future = bootstrap.bind(host, port).sync();

                //注册服务
                for (String serviceName : handlerMap.keySet()) {
                    log.info("=> [{}] - [{}]", serviceName, serverAddress);
                }

                log.info("publish services finished!");
                log.info("mrpc server start with => {}", port);
                future.channel().closeFuture().sync();
            } else {
                log.info("MRPC Server start fail!\n");
            }
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
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
        ListenableFuture<Boolean> listenableFuture = threadPoolExecutor.submit(task);

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
        }, threadPoolExecutor);
    }

}
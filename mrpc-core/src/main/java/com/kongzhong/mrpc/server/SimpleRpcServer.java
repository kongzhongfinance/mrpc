package com.kongzhong.mrpc.server;

import com.google.common.util.concurrent.*;
import com.kongzhong.mrpc.common.thread.NamedThreadFactory;
import com.kongzhong.mrpc.common.thread.RpcThreadPool;
import com.kongzhong.mrpc.config.DefaultConfig;
import com.kongzhong.mrpc.config.NettyConfig;
import com.kongzhong.mrpc.config.ServerConfig;
import com.kongzhong.mrpc.interceptor.RpcInteceptor;
import com.kongzhong.mrpc.interceptor.RpcServerInteceptor;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.registry.ServiceRegistry;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import com.kongzhong.mrpc.transport.TransferSelector;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.spi.SelectorProvider;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import static com.kongzhong.mrpc.Const.HEADER_REQUEST_ID;

/**
 * 抽象服务端请求处理器
 *
 * @author biezhi
 *         2017/4/19
 */
@Slf4j
@Data
public abstract class SimpleRpcServer {

    /**
     * RPC服务映射
     */
    protected RpcMapping rpcMapping = RpcMapping.me();

    /**
     * rpc服务地址
     */
    protected String serverAddress;

    /**
     * 弹性ip地址，不清楚可不填
     */
    protected String elasticIp;

    /**
     * 业务线程池前缀
     */
    protected String poolName = "mrpc-server";

    /**
     * 序列化类型，默认protostuff
     */
    protected RpcSerialize serialize;

    /**
     * 传输协议，默认tcp协议
     */
    protected String transport;

    /**
     * appId
     */
    protected String appId;

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
    protected List<RpcServerInteceptor> interceptorList;

    /**
     * netty服务端配置
     */
    protected NettyConfig nettyConfig;

    /**
     * 服务端处理线程池
     */
    protected static final ListeningExecutorService LISTENING_EXECUTOR_SERVICE = MoreExecutors.listeningDecorator((ThreadPoolExecutor) RpcThreadPool.getExecutor(16, -1));

    public SimpleRpcServer() {
    }

    public SimpleRpcServer(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public SimpleRpcServer(String serverAddress, ServiceRegistry serviceRegistry) {
        this.serverAddress = serverAddress;
        this.serviceRegistry = serviceRegistry;
    }

    protected void startServer() {
        if (null == transport) {
            transport = DefaultConfig.transport();
        }

        if (null == nettyConfig) {
            nettyConfig = DefaultConfig.nettyServerConfig();
        }

        if (null == serialize) {
            serialize = DefaultConfig.serialize();
        }

        transferSelector = new TransferSelector(serialize);

        ThreadFactory threadRpcFactory = new NamedThreadFactory(poolName);
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

            String[] ipAddr = serverAddress.split(":");
            if (ipAddr.length == 2) {
                //获取服务器IP地址和端口
                String host = ipAddr[0];
                int port = Integer.parseInt(ipAddr[1]);

                ServerConfig.me().setAddress(serverAddress);
                ServerConfig.me().setElasticIp(elasticIp);

                if (null != appId) {
                    ServerConfig.me().setAppId(appId);
                }

                ChannelFuture future = bootstrap.bind(host, port).sync();

                if (null == serviceRegistry) {
                    serviceRegistry = DefaultConfig.registry();
                }

                //注册服务
                for (String serviceName : rpcMapping.getServiceBeanMap().keySet()) {
                    serviceRegistry.register(serviceName);
                    log.info("Register => [{}] - [{}]", serviceName, serverAddress);
                }

                this.listenDestroy();

                log.info("publish services finished!");
                log.info("mrpc server start with => {}", port);

                future.channel().closeFuture().sync();
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

    public List<RpcServerInteceptor> getInterceptorList() {
        return interceptorList;
    }

    public void setInterceptorList(List<RpcServerInteceptor> interceptorList) {
        this.interceptorList = interceptorList;
        this.rpcMapping.addInterceptors(interceptorList);
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
                        log.debug("Request Id [{}] success.", request.getRequestId());
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
//                log.error("", t);
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
                        log.debug("Request Id [{}] success.", response.headers().get(HEADER_REQUEST_ID));
                    }

                });
            }

            @Override
            public void onFailure(Throwable t) {
                log.error("", t);
            }
        }, LISTENING_EXECUTOR_SERVICE);
    }

    /**
     * 销毁资源
     */
    protected void listenDestroy() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (String serviceName : rpcMapping.getServiceBeanMap().keySet()) {
                serviceRegistry.unregister(serviceName);
                log.debug("Unregister => [{}] - [{}]", serviceName, serverAddress);
            }
        }));
    }
}
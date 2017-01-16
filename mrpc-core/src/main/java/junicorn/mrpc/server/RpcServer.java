package junicorn.mrpc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import junicorn.mrpc.MRpcContext;
import junicorn.mrpc.common.exception.ServerInitalizserException;
import junicorn.mrpc.common.utils.RpcThreadPool;
import junicorn.mrpc.common.utils.StringUtil;
import junicorn.mrpc.inteceptor.RpcInteceptor;
import junicorn.mrpc.registry.ServiceRegistry;
import junicorn.mrpc.serialize.RpcSerialize;
import junicorn.mrpc.serialize.RpcSerializeBuilder;
import junicorn.mrpc.spring.annotation.MRpcService;
import junicorn.mrpc.spring.bean.NoInterface;
import junicorn.mrpc.spring.utils.AopTargetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * RPC Server
 */
public class RpcServer implements ApplicationContextAware, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    private String serverAddress;
    private ServiceRegistry serviceRegistry;
    private List<RpcInteceptor> inteceptorList;
    private Map<String, Object> handlerMap = new HashMap<>();

    static RpcSerialize rpcSerialize;

    private static final ThreadPoolExecutor threadPoolExecutor = RpcThreadPool.getThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2, Integer.MAX_VALUE);

    public RpcServer(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public RpcServer(String serverAddress, ServiceRegistry serviceRegistry) {
        this.serverAddress = serverAddress;
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(MRpcService.class);
        try {
            if (null != serviceBeanMap && !serviceBeanMap.isEmpty()) {
                for (Object serviceBean : serviceBeanMap.values()) {
                    Object realBean = AopTargetUtils.getTarget(serviceBean);
                    MRpcService mRpcService = realBean.getClass().getAnnotation(MRpcService.class);
                    String serviceName = mRpcService.value().getName();
                    String version = mRpcService.version();
                    if(NoInterface.class.getName().equals(serviceName)){
                        Class<?>[] intes = realBean.getClass().getInterfaces();
                        if(null == intes || intes.length != 1){
                            serviceName = realBean.getClass().getName();
                        } else {
                            serviceName = intes[0].getName();
                        }
                    }
                    if(StringUtil.isNotEmpty(version)){
                        serviceName += "_" + version;
                    }
                    handlerMap.put(serviceName, realBean);
                }
            }
        } catch (Exception e){
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new RpcServerInitializer(handlerMap, inteceptorList))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            String[] array = serverAddress.split(":");
            String host = array[0];
            int port = Integer.parseInt(array[1]);

            ChannelFuture future = bootstrap.bind(host, port).sync();
            if (serviceRegistry != null) {
                serviceRegistry.register(serverAddress);
            }

            //注册服务
            for (String serviceName : handlerMap.keySet()) {
                LOGGER.info("=> [{}] - [{}]", serviceName, serverAddress);
            }

            LOGGER.info("publish services finished!");

            MRpcContext.rpcServices = handlerMap;
            LOGGER.info("mrpc server start with => {}", port);

            future.channel().closeFuture().sync();
        } catch (Exception e){
            LOGGER.error("mrpc server start failure!!!", e);
            throw new ServerInitalizserException(e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public List<RpcInteceptor> getInteceptorList() {
        return inteceptorList;
    }

    public RpcServer setInteceptorList(List<RpcInteceptor> inteceptorList) {
        this.inteceptorList = inteceptorList;
        return this;
    }

    public void setSerialize(String serialize) {
        rpcSerialize = RpcSerializeBuilder.build(serialize);
    }

    public static void submit(Runnable task){
        threadPoolExecutor.submit(task);
    }

}

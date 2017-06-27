package com.kongzhong.mrpc.springboot.server;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.*;
import com.kongzhong.mrpc.common.thread.RpcThreadPool;
import com.kongzhong.mrpc.config.NettyConfig;
import com.kongzhong.mrpc.interceptor.RpcServerInteceptor;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.model.ServiceBean;
import com.kongzhong.mrpc.registry.ServiceRegistry;
import com.kongzhong.mrpc.server.SimpleRpcServer;
import com.kongzhong.mrpc.springboot.config.CommonProperties;
import com.kongzhong.mrpc.springboot.config.NettyProperties;
import com.kongzhong.mrpc.springboot.config.RpcServerProperties;
import com.kongzhong.mrpc.utils.StringUtils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;

import static com.kongzhong.mrpc.Const.HEADER_REQUEST_ID;
import static com.kongzhong.mrpc.Const.MRPC_SERVER_REGISTRY_PREFIX;

/**
 * RPC服务端自动配置
 *
 * @author biezhi
 *         2017/5/13
 */
@Conditional(ServerEnvironmentCondition.class)
@EnableConfigurationProperties({CommonProperties.class, RpcServerProperties.class, NettyProperties.class})
@Slf4j
public class RpcServerAutoConfigure extends SimpleRpcServer {

    @Autowired
    private CommonProperties commonProperties;

    @Autowired
    private RpcServerProperties rpcServerProperties;

    @Autowired
    private NettyProperties nettyProperties;

    @Autowired
    private ConfigurableBeanFactory configurableBeanFactory;

    /**
     * 自定义服务配置
     */
    private Map<String, Map<String, String>> customServiceMap = Maps.newHashMap();

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
    public ServiceBeanProcessor initBean() {
        log.debug("Initializing rpc service bean");
        return new ServiceBeanProcessor(rpcMapping);
    }

    @Bean
    @ConditionalOnBean(ServiceBeanProcessor.class)
    public BeanFactoryAware beanFactoryAware() {
        return (beanFactory) -> {
            log.debug("Initializing rpc server beanFactoryAware ");
            // 注册中心
            if (null != commonProperties.getRegistry() && !commonProperties.getRegistry().isEmpty()) {
                commonProperties.getRegistry().forEach((registryName, map) -> {
                    ServiceRegistry serviceRegistry = super.mapToRegistry(map);
                    serviceRegistryMap.put(registryName, serviceRegistry);
                    configurableBeanFactory.registerSingleton(MRPC_SERVER_REGISTRY_PREFIX + registryName, serviceRegistry);
                    super.usedRegistry = true;
                });
            }
            if (null != commonProperties.getCustom()) {
                customServiceMap = commonProperties.getCustom();
            }

            super.appId = rpcServerProperties.getAppId();
            super.address = rpcServerProperties.getAddress();
            super.elasticIp = rpcServerProperties.getElasticIp();
            super.poolName = rpcServerProperties.getPoolName();

            // netty参数配置
            BeanUtils.copyProperties(new NettyConfig(), nettyProperties);

            super.test = StringUtils.isNotEmpty(commonProperties.getTest()) ? commonProperties.getTest() : rpcServerProperties.getTest();

            super.transport = rpcServerProperties.getTransport();
            super.serialize = rpcServerProperties.getSerialize();
        };
    }

    @Bean
    @Order(-1)
    public CommandLineRunner rpcDaemon() {
        return args -> super.startServer();
    }

    /**
     * 获取服务暴露的地址 ip:port
     *
     * @param serviceBean
     * @return
     */
    @Override
    public String getBindAddress(ServiceBean serviceBean) {
        String address = super.getBindAddress(serviceBean);
        Map<String, String> custom = customServiceMap.get(serviceBean.getBeanName());
        if (null != custom && custom.containsKey("address")) {
            address = custom.get("address");
        }
        return address;
    }

    @Override
    public String getRegisterElasticIp(ServiceBean serviceBean) {
        String elasticIp = super.getRegisterElasticIp(serviceBean);
        Map<String, String> custom = customServiceMap.get(serviceBean.getBeanName());
        if (null != custom) {
            if (custom.containsKey("elasticIp")) {
                elasticIp = custom.get("elasticIp");
            }
            if (custom.containsKey("elastic-ip")) {
                elasticIp = custom.get("elastic-ip");
            }
        }
        return elasticIp;
    }

    /**
     * 获取服务使用的注册中心
     *
     * @param serviceBean
     * @return
     */
    @Override
    public ServiceRegistry getRegistry(ServiceBean serviceBean) {
        ServiceRegistry serviceRegistry = super.getRegistry(serviceBean);
        Map<String, String> custom = customServiceMap.get(serviceBean.getBeanName());
        if (null != custom && custom.containsKey("registry")) {
            String registryName = custom.get("registry");
            return serviceRegistryMap.get(registryName);
        }
        return serviceRegistry;
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

}
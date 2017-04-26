package com.kongzhong.mrpc.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.reflect.Reflection;
import com.kongzhong.mrpc.cluster.Connections;
import com.kongzhong.mrpc.config.ClientConfig;
import com.kongzhong.mrpc.config.Constant;
import com.kongzhong.mrpc.enums.SerializeEnum;
import com.kongzhong.mrpc.enums.TransportEnum;
import com.kongzhong.mrpc.exception.InitializeException;
import com.kongzhong.mrpc.model.ClientBean;
import com.kongzhong.mrpc.registry.ServiceDiscovery;
import com.kongzhong.mrpc.serialize.KyroSerialize;
import com.kongzhong.mrpc.serialize.ProtostuffSerialize;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import com.kongzhong.mrpc.utils.ReflectUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * rpc客户端
 */
@Data
@Slf4j
public class SimpleRpcClient {

    /**
     * rpc服务地址
     */
    protected String serverAddr;

    /**
     * 序列化类型，默认protostuff
     */
    protected String serialize = Constant.DEFAULT_SERIALIZE.name();

    /**
     * 传输协议，默认tcp协议
     */
    protected String transport = Constant.DEFAULT_TRANSPORT.name();

    /**
     * 服务发现
     */
    protected ServiceDiscovery serviceDiscovery;

    protected boolean isLoad;

    protected List<Class<?>> referers = Lists.newArrayList();

    public SimpleRpcClient() {

    }

    public SimpleRpcClient(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public SimpleRpcClient(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    public void stop() {
        Connections.me().shutdown();
    }

    /***
     * 动态代理,获得代理后的对象
     * @param rpcInterface
     * @param <T>
     * @return
     */
    public <T> T getProxyBean(Class<T> rpcInterface) {
        if (!isLoad) {
            this.init();
        }
        return (T) Reflection.newProxy(rpcInterface, new ClientProxy<T>());
    }

    private void init() {
        synchronized (Connections.class) {
            Connections connections = Connections.me();
            ClientConfig clientConfig = ClientConfig.me();

            RpcSerialize rpcSerialize = null;
            SerializeEnum serializeEnum = SerializeEnum.valueOf(serialize);
            if (null == serializeEnum) {
                throw new InitializeException("serialize type [" + serialize + "] error.");
            }

            if (serializeEnum.equals(SerializeEnum.PROTOSTUFF)) {
                clientConfig.setRpcSerialize(new ProtostuffSerialize());
            }

            if (serializeEnum.equals(SerializeEnum.KRYO)) {
                clientConfig.setRpcSerialize(new KyroSerialize());
            }

            TransportEnum transportEnum = TransportEnum.valueOf(transport.toUpperCase());
            if (null == transportEnum) {
                throw new InitializeException("transport type [" + transport + "] error.");
            }
            if (transportEnum.equals(TransportEnum.HTTP)) {
                clientConfig.setHttp(true);
            }
            clientConfig.setTransport(transportEnum);

            if (null == serviceDiscovery) {
                connections.updateNodes(Sets.newHashSet(serverAddr));
            } else {
                serviceDiscovery.discover();
            }
            isLoad = true;
        }
    }

    public void bindReferer(Class<?>... interfaces) {
        if (null != interfaces) {
            referers.addAll(Arrays.asList(interfaces));
        }
    }

    public void bindReferer(String... interfaces) {
        if (null != interfaces) {
            for (String type : interfaces) {
                referers.add(ReflectUtils.from(type));
            }
        }
    }

}
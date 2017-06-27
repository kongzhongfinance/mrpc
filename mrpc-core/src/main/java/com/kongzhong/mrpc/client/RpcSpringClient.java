package com.kongzhong.mrpc.client;

import com.google.common.collect.Sets;
import com.kongzhong.mrpc.Const;
import com.kongzhong.mrpc.client.cluster.Connections;
import com.kongzhong.mrpc.config.NettyConfig;
import com.kongzhong.mrpc.interceptor.RpcClientInteceptor;
import com.kongzhong.mrpc.model.ClientBean;
import com.kongzhong.mrpc.model.RegistryBean;
import com.kongzhong.mrpc.utils.CollectionUtils;
import com.kongzhong.mrpc.utils.StringUtils;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;

/**
 * RPC客户端
 *
 * @author biezhi
 *         2017/4/25
 */
@NoArgsConstructor
@Slf4j
public class RpcSpringClient extends SimpleRpcClient implements ApplicationContextAware, InitializingBean {

    private ApplicationContext ctx;

    @Override
    public void afterPropertiesSet() throws Exception {

        if (super.skipBind) {
            log.info("RPC client skip bind service.");
            return;
        }

        // 注册中心
        Map<String, RegistryBean> registryBeanMap = ctx.getBeansOfType(RegistryBean.class);
        if (CollectionUtils.isNotEmpty(registryBeanMap)) {
            registryBeanMap.values().forEach(registryBean -> serviceDiscoveryMap.put(registryBean.getName(), this.parseRegistry(registryBean)));
        }

        // 客户端拦截器
        Map<String, RpcClientInteceptor> inteceptorMap = ctx.getBeansOfType(RpcClientInteceptor.class);
        if (CollectionUtils.isNotEmpty(inteceptorMap)) {
            inteceptorMap.values().forEach(super::addInterceptor);
        }

        Map<String, NettyConfig> nettyConfigMap = ctx.getBeansOfType(NettyConfig.class);
        if (CollectionUtils.isNotEmpty(nettyConfigMap)) {
            this.nettyConfig = nettyConfigMap.values().stream().findFirst().get();
        }

        super.init();

        // 客户端引用
        Map<String, ClientBean> clientBeanMap = ctx.getBeansOfType(ClientBean.class);

        ConfigurableApplicationContext context = (ConfigurableApplicationContext) ctx;
        DefaultListableBeanFactory dbf = (DefaultListableBeanFactory) context.getBeanFactory();

        if (CollectionUtils.isNotEmpty(clientBeanMap)) {
            clientBeanMap.values().forEach(clientBean -> super.initReferer(clientBean, dbf));
        }

        // 初始化引用
        referers.forEach(referer -> super.initReferer(referer, dbf));

        super.directConnect();

        log.info("Bind services finished, mrpc version [{}]", Const.VERSION);
    }

    /***
     * 动态代理,获得代理后的对象
     *
     * @param rpcInterface
     * @param <T>
     * @return
     */
    public <T> T getProxyReferer(Class<T> rpcInterface) {
        if (!isInit) {
            try {
                super.init();
            } catch (Exception e) {
                log.error("RPC client init error", e);
            }
        }
        if (null == ctx && StringUtils.isNotEmpty(directAddress)) {
            // 同步直连
            Connections.me().asyncDirectConnect(rpcInterface.getName(), Sets.newHashSet(directAddress));
        }
        return this.getProxyBean(rpcInterface);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ctx = applicationContext;
    }

}
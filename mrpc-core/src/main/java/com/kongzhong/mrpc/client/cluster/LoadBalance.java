package com.kongzhong.mrpc.client.cluster;

import com.kongzhong.mrpc.client.Connections;
import com.kongzhong.mrpc.client.LocalServiceNodeTable;
import com.kongzhong.mrpc.config.ClientConfig;
import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.model.ClientBean;
import com.kongzhong.mrpc.registry.ServiceDiscovery;
import com.kongzhong.mrpc.transport.netty.SimpleClientHandler;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 负载均衡接口
 * <p>
 * Created by biezhi on 2016/12/30.
 */
public interface LoadBalance {

    /**
     * 根据服务查询连接通道
     *
     * @param serviceName
     * @return
     */
    SimpleClientHandler next(String serviceName) throws Exception;

    default List<SimpleClientHandler> handlers(String serviceName) throws Exception {
        List<SimpleClientHandler> handlers = Connections.me().getHandlers(serviceName);
        if (handlers.size() == 0) {
            TimeUnit.SECONDS.sleep(1);
            ServiceDiscovery serviceDiscovery = ClientConfig.me().getServiceDiscovery(serviceName);
            if (null != serviceDiscovery) {
                ClientBean clientBean = new ClientBean();
                clientBean.setServiceName(serviceName);
                serviceDiscovery.discover(clientBean);
                return this.handlers(serviceName);
            }
            System.out.println(String.format("Local service mappings: %s", LocalServiceNodeTable.SERVICE_MAPPINGS));
            throw new RpcException("Service [" + serviceName + "] not found.");
        }
        return handlers;
    }
}
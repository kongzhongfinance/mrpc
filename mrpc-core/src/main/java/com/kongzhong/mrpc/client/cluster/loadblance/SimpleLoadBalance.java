package com.kongzhong.mrpc.client.cluster.loadblance;

import com.kongzhong.mrpc.client.Connections;
import com.kongzhong.mrpc.client.LocalServiceNodeTable;
import com.kongzhong.mrpc.client.cluster.LoadBalance;
import com.kongzhong.mrpc.enums.LbStrategyEnum;
import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.model.ClientBean;
import com.kongzhong.mrpc.registry.ServiceDiscovery;
import com.kongzhong.mrpc.transport.netty.SimpleClientHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.LongAdder;

/**
 * 软负载简单实现(已过时)
 * <p>
 * Created by biezhi on 2016/12/30.
 */
@Slf4j
@Deprecated
public class SimpleLoadBalance implements LoadBalance {

    private Random random = new Random();

    private LbStrategyEnum lbStrategy;

    private LongAdder index = new LongAdder();

    @Deprecated
    public SimpleLoadBalance(LbStrategyEnum lbStrategyEnum) {
        this.lbStrategy = lbStrategyEnum;
    }

    private LongAdder immediatelyDiscoverCount = new LongAdder();

    public static Map<String, ServiceDiscovery> serviceDiscoveryMap;

    @Deprecated
    @Override
    public SimpleClientHandler next(String serviceName) throws Exception {
        List<SimpleClientHandler> handlers = Connections.me().getHandlers(serviceName);
        if (handlers.size() == 1) {
            immediatelyDiscoverCount = new LongAdder();
            return handlers.get(0);
        }
        if (handlers.size() == 0) {

            if (immediatelyDiscoverCount.intValue() < 3) {

                log.warn("Service [{}] not found, begin immediately discovery.", serviceName);

                // 马上服务发现
                this.immediatelyDiscovery(serviceName);
                immediatelyDiscoverCount.add(1);
                return this.next(serviceName);
            }

            log.info("Local service mappings: {}", LocalServiceNodeTable.SERVICE_MAPPINGS);
            throw new RpcException("Service [" + serviceName + "] not found.");
        }
        if (lbStrategy == LbStrategyEnum.ROUND) {
            return this.round(handlers);
        }
        if (lbStrategy == LbStrategyEnum.RANDOM) {
            return this.random(handlers);
        }
        return null;
    }

    @Deprecated
    private void immediatelyDiscovery(String serviceName) {
        ClientBean clientBean = new ClientBean();
        clientBean.setServiceName(serviceName);
        try {
            ServiceDiscovery serviceDiscovery = serviceDiscoveryMap.get("default");
            serviceDiscovery.discover(clientBean);
        } catch (Exception e) {
            log.error("Service discovery error", e);
        }
    }

    /**
     * poll load connection
     *
     * @param connections
     * @return
     */
    @Deprecated
    private SimpleClientHandler round(List<SimpleClientHandler> connections) {
        int pos = index.intValue();
        if (pos >= connections.size()) {
            index = new LongAdder();
            pos = index.intValue();
        }
        SimpleClientHandler connection = connections.get(pos);
        index.add(1);
        return connection;
    }

    /**
     * random load connection
     *
     * @param connections
     * @return
     */
    @Deprecated
    private SimpleClientHandler random(List<SimpleClientHandler> connections) {
        int randomPos = 0;
        int max = connections.size();
        if (max != 1) {
            randomPos = random.nextInt(max);
        }
        return connections.get(randomPos);
    }

    /**
     * last load connection
     *
     * @param connections
     * @return
     */
    @Deprecated
    private SimpleClientHandler last(List<SimpleClientHandler> connections) {
        return connections.get(connections.size() - 1);
    }

}
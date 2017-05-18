package com.kongzhong.mrpc.client.cluster.loadblance;

import com.kongzhong.mrpc.client.RpcInvoker;
import com.kongzhong.mrpc.client.cluster.Connections;
import com.kongzhong.mrpc.config.ClientConfig;
import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.transport.SimpleClientHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * 软负载简单实现
 * <p>
 * Created by biezhi on 2016/12/30.
 */
@Slf4j
public class SimpleLoadBalance implements LoadBalance {

    private AtomicInteger posInt = new AtomicInteger(0);
    private Random random = new Random();

    /**
     * 性能优于AtomicInteger（JDK8出现）
     */
    private LongAdder posLong = new LongAdder();

    @Override
    public RpcInvoker getInvoker(String serviceName) {
        try {
            LBStrategy LBStrategy = ClientConfig.me().getLbStrategy();
            List<SimpleClientHandler> handlers = Connections.me().getHandlers(serviceName);
            if (handlers.size() == 1) {
                return new RpcInvoker(handlers.get(0));
            }
            if (LBStrategy == LBStrategy.ROUND) {
                return new RpcInvoker(this.round(handlers));
            }
            if (LBStrategy == LBStrategy.RANDOM) {
                return new RpcInvoker(this.random(handlers));
            }
            if (LBStrategy == LBStrategy.LAST) {
                return new RpcInvoker(this.last(handlers));
            }
        } catch (Exception e) {
            throw new RpcException(e);
        }
        return null;
    }

    /**
     * poll load connection
     *
     * @param connections
     * @return
     */
    private SimpleClientHandler round(List<SimpleClientHandler> connections) {
        int pos = posInt.get();
        if (pos >= connections.size()) {
            posInt.set(0);
            pos = posInt.get();
        }
        SimpleClientHandler connection = connections.get(pos);
        posInt.addAndGet(1);
        return connection;
    }

    /**
     * random load connection
     *
     * @param connections
     * @return
     */
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
    private SimpleClientHandler last(List<SimpleClientHandler> connections) {
        return connections.get(connections.size() - 1);
    }


}
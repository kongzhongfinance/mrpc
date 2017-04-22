package com.kongzhong.mrpc.ha.loadblance;

import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.ha.Connections;
import com.kongzhong.mrpc.model.ClientConfig;
import com.kongzhong.mrpc.model.Strategy;
import com.kongzhong.mrpc.transport.SimpleClientHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by biezhi on 2016/12/30.
 */
@Slf4j
public class SimpleLoadBalance implements LoadBalance {

    private AtomicInteger posInt = new AtomicInteger(0);
    private Random random = new Random();
    private CountDownLatch latch = new CountDownLatch(1);

    @Override
    public SimpleClientHandler getClientHandler() {
        try {
            Strategy strategy = ClientConfig.me().getStrategy();
            List<SimpleClientHandler> handlers = Connections.me().getHandlers();
            if (handlers.size() == 1) {
                return handlers.get(0);
            }
            if (strategy == Strategy.POLL) {
                return this.poll(handlers);
            }
            if (strategy == Strategy.RANDOM) {
                return this.random(handlers);
            }
            if (strategy == Strategy.LAST) {
                return this.last(handlers);
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
    private SimpleClientHandler poll(List<SimpleClientHandler> connections) {
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
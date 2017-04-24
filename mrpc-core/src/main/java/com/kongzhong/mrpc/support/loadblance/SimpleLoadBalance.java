package com.kongzhong.mrpc.support.loadblance;

import com.google.common.collect.Lists;
import com.kongzhong.mrpc.client.RpcInvoker;
import com.kongzhong.mrpc.config.ClientConfig;
import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.support.Connections;
import com.kongzhong.mrpc.support.LBStrategy;
import com.kongzhong.mrpc.transport.SimpleClientHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 软负载简单实现
 * <p>
 * Created by biezhi on 2016/12/30.
 */
@Slf4j
public class SimpleLoadBalance implements LoadBalance {

    private AtomicInteger posInt = new AtomicInteger(0);
    private Random random = new Random();
    private CountDownLatch latch = new CountDownLatch(1);

    @Override
    public RpcInvoker getInvoker() {
        try {
            LBStrategy LBStrategy = ClientConfig.me().getLbStrategy();
            List<SimpleClientHandler> handlers = Connections.me().getHandlers();
            if (handlers.size() == 1) {
                return new RpcInvoker(handlers.get(0));
            }
            if (LBStrategy == LBStrategy.POLL) {
                return new RpcInvoker(this.poll(handlers));
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
     * 读取一组连接执行器，此处应实现更具体的负载均衡策略
     *
     * @return
     */
    @Override
    public List<RpcInvoker> getInvokers() {
        try {
            List<RpcInvoker> result = Lists.newCopyOnWriteArrayList();
            Connections.me().getHandlers().forEach(clientHandler -> {
                result.add(getRpcReferer(clientHandler));
            });
            return result;
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    private RpcInvoker getRpcReferer(SimpleClientHandler clientHandler) {
        RpcInvoker rpcInvoker = new RpcInvoker(clientHandler);
        return rpcInvoker;
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
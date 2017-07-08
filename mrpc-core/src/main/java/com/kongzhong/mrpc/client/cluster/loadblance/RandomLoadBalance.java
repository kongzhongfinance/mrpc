package com.kongzhong.mrpc.client.cluster.loadblance;

import com.kongzhong.mrpc.client.Connections;
import com.kongzhong.mrpc.client.cluster.LoadBalance;
import com.kongzhong.mrpc.transport.netty.SimpleClientHandler;

import java.util.List;
import java.util.Random;

/**
 * 随机
 * <p>
 * Created by biezhi on 09/07/2017.
 */
public class RandomLoadBalance implements LoadBalance {

    private Random random = new Random();

    @Override
    public SimpleClientHandler next(String serviceName) throws Exception {
        List<SimpleClientHandler> handlers = Connections.me().getHandlers(serviceName);
        if (handlers.size() == 1) {
            return handlers.get(0);
        }
        int randomPos = 0;
        int max = handlers.size();
        if (max != 1) {
            randomPos = random.nextInt(max);
        }
        return handlers.get(randomPos);
    }

}

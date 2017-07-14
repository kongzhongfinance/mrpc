package com.kongzhong.mrpc.client.cluster.loadblance;

import com.kongzhong.mrpc.client.cluster.LoadBalance;
import com.kongzhong.mrpc.transport.netty.SimpleClientHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 加权随机策略
 * <p>
 * Created by biezhi on 14/07/2017.
 */
@Slf4j
public class WeightRandomStrategy implements LoadBalance {

    @Override
    public SimpleClientHandler next(String serviceName) throws Exception {
        List<SimpleClientHandler> handlers = handlers(serviceName);
        if (handlers.size() == 1) {
            return handlers.get(0);
        }
        List<SimpleClientHandler> serverList = new ArrayList<>();
        handlers.forEach(handler -> {
            int weight = handler.getNettyClient().getWeight();
            for (int i = 0; i < weight; i++) {
                serverList.add(handler);
            }
        });
        Random random    = new Random();
        int    randomPos = random.nextInt(serverList.size());
        return serverList.get(randomPos);
    }

}

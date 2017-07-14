package com.kongzhong.mrpc.client.cluster.loadblance;

import com.kongzhong.mrpc.client.cluster.LoadBalance;
import com.kongzhong.mrpc.transport.netty.SimpleClientHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

/**
 * 加权轮循策略
 * <p>
 * Created by biezhi on 14/07/2017.
 */
@Slf4j
public class WeightRoundRobinStrategy implements LoadBalance {

    private LongAdder pos = new LongAdder();

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
        if (pos.intValue() > handlers.size()) {
            pos = new LongAdder();
        }
        SimpleClientHandler simpleClientHandler = serverList.get(pos.intValue());
        pos.add(1);
        return simpleClientHandler;
    }

}

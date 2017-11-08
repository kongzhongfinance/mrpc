package com.kongzhong.mrpc.client.cluster.loadblance;

import com.kongzhong.mrpc.client.cluster.LoadBalance;
import com.kongzhong.mrpc.transport.netty.SimpleClientHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.LongAdder;

/**
 * 轮循
 * <p>
 * Created by biezhi on 09/07/2017.
 */
@Slf4j
public class RoundRobinStrategy implements LoadBalance {

    private LongAdder index = new LongAdder();

    @Override
    public SimpleClientHandler next(String appId, String serviceName) throws Exception {
        List<SimpleClientHandler> handlers = handlers(appId, serviceName);
        if (handlers.size() == 1) {
            return handlers.get(0);
        }
        int pos = index.intValue();
        if (pos >= handlers.size()) {
            index = new LongAdder();
            pos = index.intValue();
        }
        index.add(1);
        return handlers.get(pos);
    }

}

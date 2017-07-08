package com.kongzhong.mrpc.client.cluster.loadblance;

import com.kongzhong.mrpc.client.Connections;
import com.kongzhong.mrpc.client.cluster.LoadBalance;
import com.kongzhong.mrpc.transport.netty.SimpleClientHandler;

import java.util.List;
import java.util.concurrent.atomic.LongAdder;

/**
 * 轮询
 * <p>
 * Created by biezhi on 09/07/2017.
 */
public class RoundRobinLoadBalance implements LoadBalance {

    private LongAdder index = new LongAdder();

    @Override
    public SimpleClientHandler next(String serviceName) throws Exception {
        List<SimpleClientHandler> handlers = Connections.me().getHandlers(serviceName);
        if (handlers.size() == 1) {
            return handlers.get(0);
        }

        int pos = index.intValue();
        if (pos >= handlers.size()) {
            index = new LongAdder();
            pos = index.intValue();
        }
        SimpleClientHandler connection = handlers.get(pos);
        index.add(1);
        return connection;
    }

}

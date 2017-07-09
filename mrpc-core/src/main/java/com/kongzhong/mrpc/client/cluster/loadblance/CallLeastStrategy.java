package com.kongzhong.mrpc.client.cluster.loadblance;

import com.kongzhong.mrpc.client.cluster.LoadBalance;
import com.kongzhong.mrpc.transport.netty.SimpleClientHandler;

import java.util.Comparator;
import java.util.List;

/**
 * 最小连接
 * <p>
 * Created by biezhi on 09/07/2017.
 */
public class CallLeastStrategy implements LoadBalance {

    @Override
    public SimpleClientHandler next(String serviceName) throws Exception {
        List<SimpleClientHandler> handlers = handlers(serviceName);
        return handlers.stream()
                .sorted(Comparator.comparingLong(SimpleClientHandler::getHits))
                .findFirst().get();
    }

}

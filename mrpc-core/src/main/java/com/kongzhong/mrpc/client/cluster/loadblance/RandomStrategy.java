package com.kongzhong.mrpc.client.cluster.loadblance;

import com.kongzhong.mrpc.client.cluster.LoadBalance;
import com.kongzhong.mrpc.transport.netty.SimpleClientHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;

/**
 * 随机
 * <p>
 * Created by biezhi on 09/07/2017.
 */
@Slf4j
public class RandomStrategy implements LoadBalance {

    private Random random = new Random();

    @Override
    public SimpleClientHandler next(String serviceName) throws Exception {
        List<SimpleClientHandler> handlers = handlers(serviceName);
        return handlers.get(random.nextInt(handlers.size()));
    }

}

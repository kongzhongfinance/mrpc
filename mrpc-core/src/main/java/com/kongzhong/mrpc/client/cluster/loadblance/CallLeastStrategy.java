package com.kongzhong.mrpc.client.cluster.loadblance;

import com.kongzhong.mrpc.client.cluster.LoadBalance;
import com.kongzhong.mrpc.transport.http.HttpClientHandler;

import java.util.Comparator;
import java.util.List;

/**
 * 最小连接
 * <p>
 * Created by biezhi on 09/07/2017.
 */
public class CallLeastStrategy implements LoadBalance {

    @Override
    public HttpClientHandler next(String appId, String serviceName) throws Exception {
        List<HttpClientHandler> handlers = handlers(appId, serviceName);
        return handlers.stream()
                .sorted(Comparator.comparingLong(HttpClientHandler::getHits))
                .findFirst().get();
    }

}

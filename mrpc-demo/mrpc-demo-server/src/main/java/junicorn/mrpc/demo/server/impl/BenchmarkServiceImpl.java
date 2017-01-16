package junicorn.mrpc.demo.server.impl;

import junicorn.mrpc.demo.api.BenchmarkService;
import junicorn.mrpc.spring.annotation.MRpcService;

import java.util.List;
import java.util.Map;

/**
 * Created by biezhi on 2017/1/10.
 */
@MRpcService(value = BenchmarkService.class)
public class BenchmarkServiceImpl implements BenchmarkService {

    @Override
    public Object echoService(Object request) {
        return request;
    }

    @Override
    public void emptyService() {
    }

    @Override
    public Map<Long, Integer> getUserTypes(List<Long> uids) {
        return null;
    }

    @Override
    public long[] getLastStausIds(long[] uids) {
        return new long[0];
    }

}

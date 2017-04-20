
package com.kongzhong.mrpc.test.service;

import com.kongzhong.mrpc.spring.annotation.MRpcService;

import java.util.List;
import java.util.Map;

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

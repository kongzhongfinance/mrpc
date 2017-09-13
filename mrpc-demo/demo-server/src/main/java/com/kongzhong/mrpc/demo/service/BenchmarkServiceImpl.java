
package com.kongzhong.mrpc.demo.service;

import com.kongzhong.mrpc.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
@RpcService
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

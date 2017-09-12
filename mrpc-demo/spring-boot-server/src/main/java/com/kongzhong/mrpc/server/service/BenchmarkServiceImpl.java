
package com.kongzhong.mrpc.server.service;

import com.kongzhong.mrpc.annotation.RpcService;
import com.kongzhong.mrpc.demo.service.BenchmarkService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
@RpcService
public class BenchmarkServiceImpl implements BenchmarkService {

    @Override
    public Object echoService(Object request) {
        log.info("####echoService####");
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

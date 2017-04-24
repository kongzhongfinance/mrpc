package com.kongzhong.mrpc.demo.service;

import java.util.List;
import java.util.Map;

/**
 * Created by biezhi on 2017/1/10.
 */
public interface BenchmarkService {

    Object echoService(Object request);

    void emptyService();

    Map<Long, Integer> getUserTypes(List<Long> uids);

    long[] getLastStausIds(long[] uids);

}
package com.kongzhong.mrpc.demo.benchmark;

import com.kongzhong.mrpc.demo.service.BenchmarkService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ThreadLocalRandom;

public class TestStringRunnable extends AbstractClientRunnable {

    private String str;

    public TestStringRunnable(BenchmarkService service, String params, CyclicBarrier barrier, CountDownLatch latch, long startTime, long endTime) {
        super(service, barrier, latch, startTime, endTime);
        int size = Integer.parseInt(params);
        int length = 1024 * size;
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append((char) (ThreadLocalRandom.current().nextInt(33, 128)));
        }
        str = builder.toString();
    }

    @Override
    protected Object call(BenchmarkService benchmarkService) {
        Object result = benchmarkService.echoService(str);
        return result;
    }
}
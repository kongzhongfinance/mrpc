package junicorn.mrpc.demo.client.benchmark;

import junicorn.mrpc.demo.api.BenchmarkService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class TestEmptyRunnable extends AbstractClientRunnable {

    public TestEmptyRunnable(BenchmarkService service, String params, CyclicBarrier barrier, CountDownLatch latch, long startTime, long endTime) {
        super(service, barrier, latch, startTime, endTime);
    }

    @Override
    protected Object call(BenchmarkService benchmarkService) {
        benchmarkService.emptyService();
        return "empty";
    }
}
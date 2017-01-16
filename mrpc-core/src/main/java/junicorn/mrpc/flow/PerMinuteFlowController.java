package junicorn.mrpc.flow;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import junicorn.mrpc.common.utils.NamedThreadFactory;
import junicorn.mrpc.common.utils.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerMinuteFlowController {

    private static final Logger logger = LoggerFactory.getLogger(PerMinuteFlowController.class);

    private static Map<String, FlowController> serviceFlowController = new HashMap<String, FlowController>();

    private static final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("provider-timer"));

    public static void main(String[] args) throws InterruptedException {

        Thread t = new Thread(new MetricsScanner(), "timeout.scanner");
        t.setDaemon(true);
        t.start();

        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("清理下一个时间的槽位");
                    clearNextMinuteCallCount();
                } catch (Exception e) {
                    logger.warn("schedule publish failed [{}]", e.getMessage());
                }
            }

        }, 3, 45, TimeUnit.SECONDS);

        for (int i = 0; i < 10000; i++) {
            callXXXXService("TEST.SERVICE");
            Thread.sleep(20l);
        }
    }

    private static void clearNextMinuteCallCount() {

        for (String str : serviceFlowController.keySet()) {
            FlowController flowController = serviceFlowController.get(str);
            flowController.clearNextMinuteCallCount();
        }
    }

    private static void callXXXXService(String service) {

        FlowController controller = serviceFlowController.get(service);
        if (null == controller) {
            controller = new FlowController();
            serviceFlowController.put(service, controller);
        }
        controller.incrementAtCurrentMinute();

        //调用的核心逻辑处理

    }

    public static class FlowController {

        private AtomicLong[] metricses = new AtomicLong[]{new AtomicLong(0), new AtomicLong(0), new AtomicLong(0)};


        public void incrementAtCurrentMinute() {

            long currentTime = SystemClock.millisClock().now();
            int index = (int) ((currentTime / 60000) % 3);

            AtomicLong atomicLong = metricses[index];
            atomicLong.incrementAndGet();

        }

        public long getLastCallCountAtLastMinute() {

            long currentTime = SystemClock.millisClock().now();
            int index = (int) (((currentTime / 60000) - 1) % 3);
            AtomicLong atomicLong = metricses[index];
            return atomicLong.get();

        }

        public long getCurrentCallCount() {

            long currentTime = SystemClock.millisClock().now();
            int index = (int) (((currentTime / 60000)) % 3);
            AtomicLong atomicLong = metricses[index];
            return atomicLong.get();

        }

        public long getNextMinuteCallCount() {

            long currentTime = SystemClock.millisClock().now();
            int index = (int) (((currentTime / 60000) + 1) % 3);
            AtomicLong atomicLong = metricses[index];
            return atomicLong.get();

        }

        public void clearNextMinuteCallCount() {

            System.out.println("清理开始");
            long currentTime = SystemClock.millisClock().now();
            int index = (int) (((currentTime / 60000) + 1) % 3);
            AtomicLong atomicLong = metricses[index];
            atomicLong.set(0);
        }

        public AtomicLong[] getMetricses() {
            return metricses;
        }

        public void setMetricses(AtomicLong[] metricses) {
            this.metricses = metricses;
        }

    }

    public static class MetricsScanner implements Runnable {

        @Override
        public void run() {
            for (; ; ) {
                logger.info("统计中");
                try {
                    Thread.sleep(5000);
                    for (String str : serviceFlowController.keySet()) {
                        FlowController flowController = serviceFlowController.get(str);
                        logger.info("上一分钟调用的次数是[{}]", flowController.getLastCallCountAtLastMinute());
                        logger.info("当前分钟调用的次数是[{}]", flowController.getCurrentCallCount());
                        logger.info("下以分钟调用的次数是[{}]", flowController.getNextMinuteCallCount());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
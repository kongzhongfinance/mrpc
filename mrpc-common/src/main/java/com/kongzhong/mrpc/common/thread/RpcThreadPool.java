package com.kongzhong.mrpc.common.thread;

import java.util.concurrent.*;

/**
 * 自定义的线程池
 */
public class RpcThreadPool {

    private static BlockingQueue<Runnable> createBlockingQueue(int queues) {
        BlockingQueueType queueType = BlockingQueueType.fromString("LinkedBlockingQueue");
        switch (queueType) {
            case LINKED_BLOCKING_QUEUE:
                return new LinkedBlockingQueue<>();
            case ARRAY_BLOCKING_QUEUE:
                return new ArrayBlockingQueue<>(Runtime.getRuntime().availableProcessors() * queues);
            case SYNCHRONOUS_QUEUE:
                return new SynchronousQueue<>();
            default:
                return null;
        }
    }

    /**
     * @param threads 固定数量线程的线程池
     * @param queues  设置线程池工作队列,  0:同步队列
     *                <0 无界队列 LinkedBlockingQueue
     *                >0 无界队列,指定了初始容量
     * @return 返回指定了线程池之后的线程池执行器
     */
    public static Executor getExecutor(int threads, int queues) {
        String name = "mrpc-pool";
        return new ThreadPoolExecutor(threads, threads, 0, TimeUnit.MILLISECONDS,
                queues == 0 ? new SynchronousQueue<>()
                        : (queues < 0 ? new LinkedBlockingQueue<>()
                        : new LinkedBlockingQueue<>(queues)),
                new NamedThreadFactory(name, true), new AbortPolicyWithReport(name));
    }

}

package com.kongzhong.mrpc.common.thread;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 自定义的线程池
 */
public class RpcThreadPool {

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

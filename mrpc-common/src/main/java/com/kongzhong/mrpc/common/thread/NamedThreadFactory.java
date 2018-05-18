package com.kongzhong.mrpc.common.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程工厂:实际上就是对Runable进行一个包装,对线程设置一些信息和监控信息
 */
public class NamedThreadFactory implements ThreadFactory {

    private final AtomicInteger mThreadNum = new AtomicInteger(1);

    /**
     * 前缀
     */
    private final String prefix;

    /**
     * 是否是后台线程, 默认false不是后台线程
     */
    private final boolean daemoThread;

    /**
     * 线程组
     */
    private final ThreadGroup threadGroup;

    /**
     * 构造器
     */
    public NamedThreadFactory(String prefix) {
        this(prefix, false);
    }

    /**
     * 构造器
     */
    public NamedThreadFactory(String prefix, boolean daemo) {
        this.prefix = prefix + "-thread-";
        daemoThread = daemo;
        SecurityManager s = System.getSecurityManager();
        threadGroup = (s == null) ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
    }

    /**
     * 核心函数:新建一个线程
     *
     * @param runnable 传入一个任务task
     * @return
     */
    @Override
    public Thread newThread(Runnable runnable) {
        String name = prefix + mThreadNum.getAndIncrement();
        Thread ret = new Thread(threadGroup, runnable, name, 0);
        ret.setDaemon(daemoThread);
        return ret;
    }

}
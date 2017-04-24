package com.kongzhong.mrpc.metric;

import lombok.Data;
import lombok.ToString;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author biezhi
 *         2017/4/24
 */
@Data
@ToString
public class Metric {

    private int minTime;
    private int maxTime;
    private int totalTime;
    private AtomicLong tps = new AtomicLong(0);
    private Lock lock = new ReentrantLock();

    public long incrementAndGetTPS() {
        return tps.incrementAndGet();
    }

    public long getAndSet() {
        totalTime = 0;
        return tps.getAndSet(0);
    }

    public void expendTime(long expendTime) {
        lock.lock();
        try {
            if (expendTime < minTime || minTime == 0) {
                minTime = (int) expendTime;
            }
            if (expendTime > maxTime) {
                maxTime = (int) expendTime;
            }
            totalTime += expendTime;
        } finally {
            lock.unlock();
        }
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

}

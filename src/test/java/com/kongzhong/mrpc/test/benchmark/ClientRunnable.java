package com.kongzhong.mrpc.test.benchmark;

public interface ClientRunnable extends Runnable {

    RunnableStatistics getStatistics();
}
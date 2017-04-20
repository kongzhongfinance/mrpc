package com.kongzhong.mrpc.demo.benchmark;

public interface ClientRunnable extends Runnable {

    RunnableStatistics getStatistics();
}
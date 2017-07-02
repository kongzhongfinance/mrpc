package com.kongzhong.mrpc.demo.benchmark;

public class RunnableStatistics {
    public int statisticTime;
    // Transaction per second
    public long[] TPS;
    // response times per second
    public long[] RT;
    // error Transaction per second
    public long[] errTPS;
    // error response times per second
    public long[] errRT;

    public long above0sum;      // [0,1]
    public long above1sum;      // (1,5]
    public long above5sum;      // (5,10]
    public long above10sum;     // (10,50]
    public long above50sum;     // (50,100]
    public long above100sum;    // (100,500]
    public long above500sum;    // (500,1000]
    public long above1000sum;   // > 1000

    public RunnableStatistics(int statisticTime) {
        this.statisticTime = statisticTime;
        TPS = new long[statisticTime];
        RT = new long[statisticTime];
        errTPS = new long[statisticTime];
        errRT = new long[statisticTime];
    }
}
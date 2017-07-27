package com.kongzhong.mrpc.trace.custom;


/**
 * TraceResult
 */
public class TraceResult {

    private Object result;

    /**
     * The sr time
     */
    private long srt;

    /**
     * The ss time
     */
    private long sst;

    public TraceResult(Object result, long srt, long sst) {
        this.result = result;
        this.srt = srt;
        this.sst = sst;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public long getSrt() {
        return srt;
    }

    public void setSrt(long srt) {
        this.srt = srt;
    }

    public long getSst() {
        return sst;
    }

    public void setSst(long sst) {
        this.sst = sst;
    }
}

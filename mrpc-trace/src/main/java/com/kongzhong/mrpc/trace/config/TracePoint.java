package com.kongzhong.mrpc.trace.config;

/**
 * TracePoint
 */
public class TracePoint {

    /**
     * The trace key
     */
    private String key;

    /**
     * The trace url pattern
     */
    private String pattern;

    /**
     * The trace description;
     */
    private String desc;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "TracePoint{" +
                "key='" + key + '\'' +
                ", pattern='" + pattern + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }
}

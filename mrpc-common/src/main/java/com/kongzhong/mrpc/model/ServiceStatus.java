package com.kongzhong.mrpc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.concurrent.atomic.LongAdder;

@NoArgsConstructor
@Data
@ToString
public class ServiceStatus implements Serializable {

    private String appId;
    private String serviceName;
    private String address;
    private String elasticIp;
    private String registry;
    private String version;

    private long invokeCount;
    private long successCount;
    private long errorCount;
    private long timeoutCount;
    private long clientCount;

    @JsonIgnore
    private LongAdder invokeAdder  = new LongAdder();
    @JsonIgnore
    private LongAdder successAdder = new LongAdder();
    @JsonIgnore
    private LongAdder errorAdder   = new LongAdder();
    @JsonIgnore
    private LongAdder timeoutAdder = new LongAdder();
    @JsonIgnore
    private LongAdder clientAdder  = new LongAdder();

    public void addSuccess() {
        invokeAdder.add(1);
        successAdder.add(1);
        invokeCount = invokeAdder.longValue();
        successCount = successAdder.longValue();
    }

    public void addError() {
        invokeAdder.add(1);
        errorAdder.add(1);
        invokeCount = invokeAdder.longValue();
        errorCount = errorAdder.longValue();
    }

    public void addTimeout() {
        invokeAdder.add(1);
        timeoutAdder.add(1);
        invokeCount = invokeAdder.longValue();
        timeoutCount = timeoutAdder.longValue();
    }

    public void addClient() {
        clientAdder.add(1);
        clientCount = clientAdder.longValue();
    }

    public void removeClient() {
        clientAdder.add(-1);
        clientCount = clientAdder.longValue();
    }

}
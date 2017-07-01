package com.kongzhong.mrpc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.io.Serializable;
import java.util.concurrent.atomic.LongAdder;

@ManagedResource
@NoArgsConstructor
@ToString
public class ServiceStatus implements Serializable {

    private String appId;
    private String serviceName;
    private String address;
    private String elasticIp;
    private String registry;
    private String version;

    private boolean isAlive;
    private long invokeCount;
    private long successCount;
    private long errorCount;
    private long timeoutCount;

    @JsonIgnore
    private LongAdder invokeAdder = new LongAdder();
    @JsonIgnore
    private LongAdder successAdder = new LongAdder();
    @JsonIgnore
    private LongAdder errorAdder = new LongAdder();
    @JsonIgnore
    private LongAdder timeoutAdder = new LongAdder();

    @ManagedOperation
    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

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

    @ManagedOperation
    public long getInvokeCount() {
        return invokeCount;
    }

    public void setInvokeCount(long invokeCount) {
        this.invokeCount = invokeCount;
    }

    @ManagedOperation
    public long getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(long successCount) {
        this.successCount = successCount;
    }

    @ManagedOperation
    public long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(long errorCount) {
        this.errorCount = errorCount;
    }

    @ManagedOperation
    public long getTimeoutCount() {
        return timeoutCount;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getElasticIp() {
        return elasticIp;
    }

    public void setElasticIp(String elasticIp) {
        this.elasticIp = elasticIp;
    }

    public String getRegistry() {
        return registry;
    }

    public void setRegistry(String registry) {
        this.registry = registry;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setTimeoutCount(long timeoutCount) {
        this.timeoutCount = timeoutCount;
    }
}
package com.kongzhong.mrpc.model;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * 服务元数据
 *
 * @author biezhi
 *         2017/4/21
 */
public class ServiceMeta implements Serializable {

    private String serviceName;

    private String httpMethod;

    private Method method;

    public ServiceMeta(String serviceName, Method method, String httpMethod) {
        this.serviceName = serviceName;
        this.method = method;
        this.httpMethod = httpMethod;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceMeta that = (ServiceMeta) o;

        if (serviceName != null ? !serviceName.equals(that.serviceName) : that.serviceName != null) return false;
        if (method != null ? !method.equals(that.method) : that.method != null) return false;
        if (httpMethod != null ? !httpMethod.equals(that.httpMethod) : that.httpMethod != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = serviceName != null ? serviceName.hashCode() : 0;
        result = 31 * result + (method != null ? method.hashCode() : 0);
        result = 31 * result + (httpMethod != null ? httpMethod.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "[" +
                "serviceName='" + serviceName + '\'' +
                ", method=" + method +
                ", httpMethod='" + httpMethod + '\'' +
                ']';
    }
}

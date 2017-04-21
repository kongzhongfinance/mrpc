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

    private String contentType;

    private Method method;

    public ServiceMeta(String serviceName, Method method, String contentType, String httpMethod) {
        this.serviceName = serviceName;
        this.method = method;
        this.contentType = contentType;
        this.httpMethod = httpMethod;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}

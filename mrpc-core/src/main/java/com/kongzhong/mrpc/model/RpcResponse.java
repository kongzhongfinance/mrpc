package com.kongzhong.mrpc.model;

/**
 * 响应
 */
public class RpcResponse {

    private String requestId;
    private Object result;
    private Throwable exception;

    public RpcResponse() {
    }

    public RpcResponse(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    @Override
    public String toString() {
        return "[requestId=" + requestId +
                ", result=" + result +
                ", exp=" + exception + ']';
    }
}
package com.kongzhong.mrpc.model;

/**
 * @author biezhi
 *         2017/4/21
 */
public class RpcRet {

    private int code;
    private String msg;
    private int time;
    private Object data;

    public RpcRet(int code, String msg) {
        this.code = code;
        this.msg = msg;
        long unixTime = System.currentTimeMillis() / 1000L;
        this.time = (int) unixTime;
    }

    public RpcRet(int code, String msg, int time) {
        this.code = code;
        this.msg = msg;
        this.time = time;
    }

    public RpcRet(int code, Object data) {
        this.code = code;
        long unixTime = System.currentTimeMillis() / 1000L;
        this.time = (int) unixTime;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RpcRet that = (RpcRet) o;

        if (code != that.code) return false;
        if (time != that.time) return false;
        if (msg != null ? !msg.equals(that.msg) : that.msg != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = code;
        result = 31 * result + (msg != null ? msg.hashCode() : 0);
        result = 31 * result + time;
        return result;
    }

    public static RpcRet ok(Object data) {
        return new RpcRet(200, data);
    }

    public static RpcRet notFound(String msg) {
        return new RpcRet(404, msg);
    }

    public static RpcRet notAllowMethod(String msg) {
        return new RpcRet(405, msg);
    }

    public static RpcRet error(String msg) {
        return new RpcRet(500, msg);
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}

package com.kongzhong.mrpc.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author biezhi
 *         2017/4/21
 */
@Data
@NoArgsConstructor
@ToString
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

}

package com.kongzhong.mrpc.config;

import com.kongzhong.mrpc.enums.TransportEnum;

/**
 * Created by biezhi on 2016/10/14.
 */
public interface Constant {

    String ZK_ROOT = "/mrpc";

    int ZK_SESSION_TIMEOUT = 5000;

    int RPC_RESPONSE_TIMEOUT = 5000;

    int RPC_RETRY_COUNT = 3;

}
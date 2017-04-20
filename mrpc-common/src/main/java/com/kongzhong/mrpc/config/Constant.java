package com.kongzhong.mrpc.config;

/**
 * Created by biezhi on 2016/10/14.
 */
public interface Constant {

    String ZK_REGISTRY_PATH = "/registry";

    String ZK_MRPC_PATH = ZK_REGISTRY_PATH + "/mrpc";

    int ZK_SESSION_TIMEOUT = 5000;

    int RPC_RESPONSE_TIMEOUT = 5000;

    int RPC_RETRY_COUNT = 3;

    String PROTOSTUFF = "protostuff";

    String JSON = "json";

    String KRYO = "kryo";

}
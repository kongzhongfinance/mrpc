package com.kongzhong.mrpc.admin.config;

import com.kongzhong.mrpc.model.ServiceNodePayload;
import com.kongzhong.mrpc.transport.netty.SimpleClientHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * RPC Admin常量
 * <p>
 * Created by biezhi on 01/07/2017.
 */
public class RpcAdminConst {

    public static final String LOGIN_SESSION_KEY = "login_user";
    public static final String LOGIN_COOKIE_KEY = "RPC_ADMIN_USER";

    public static final Set<ServiceNodePayload> serviceNodePayloads = new HashSet<>();
    public static final Map<String, SimpleClientHandler> clientHandlerMap = new HashMap<>();

}

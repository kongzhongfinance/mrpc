package com.kongzhong.mrpc.admin.config;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;

/**
 * RPC Admin常量
 * <p>
 * Created by biezhi on 01/07/2017.
 */
public class RpcAdminConst {

    public static final String LOGIN_SESSION_KEY = "login_user";
    public static final String LOGIN_COOKIE_KEY = "RPC_ADMIN_USER";

    public static final String RPC_SERVICE_STATUS_JMX_KEY = "com.kongzhong.mrpc.mbean:name=serviceStatusTable,type=ServiceStatusTable";

    /**
     * JMX连接器
     */
    public static JMXConnector jmxConnector;

    /**
     * JMX服务连接
     */
    public static MBeanServerConnection mBeanServerConnection;

}

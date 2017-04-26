package com.kongzhong.mrpc.registry;

public class ZkConf {

    private static ZkConf zkconf;
    private String address;
    private int sessionTimeout;
    private int connectionTimeout;

    public String getAddress() {
        return address;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public ZkConf(String address, int sessionTimeout, int connectionTimeout) {
        this.address = address;
        this.sessionTimeout = sessionTimeout;
        this.connectionTimeout = connectionTimeout;
        zkconf = this;
    }

    public static ZkConf getZkconf() {
        return zkconf;
    }
}
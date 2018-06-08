package com.kongzhong.mrpc.model;

import lombok.Data;

import java.util.Set;

@Data
public class RpcServer {

    private Long    id;
    private String  appId;
    private String  host;
    private Integer port;
    private Integer pid;
    private String  status;
    private String  owner;
    private String  ownerEmail;

    private String onlineTime;
    private String offlineTime;

    private Set<String> services;
}
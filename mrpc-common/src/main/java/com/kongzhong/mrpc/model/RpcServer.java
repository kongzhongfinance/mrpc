package com.kongzhong.mrpc.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RpcServer {

    private Long          id;
    private String        appId;
    private String        appAlias;
    private String        host;
    private Integer       port;
    private Integer       pid;
    private String        status;
    private String        owner;
    private String        ownerEmail;
    private LocalDateTime onlineTime;
    private LocalDateTime offlineTime;
    private LocalDateTime updatedTime;

}
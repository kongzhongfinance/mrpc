package com.kongzhong.mrpc.model;

import lombok.Data;

import java.util.Set;

/**
 * @author biezhi
 * @date 2018/6/11
 */
@Data
public class RpcClientNotice {

    private String  appId;
    private String  host;
    private Integer pid;
    private String  onlineTime;
    private String  offlineTime;
    private String        owner;
    private String        ownerEmail;

    private Set<String> services;

}

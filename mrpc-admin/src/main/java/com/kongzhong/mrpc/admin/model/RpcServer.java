package com.kongzhong.mrpc.admin.model;

import io.github.biezhi.anima.Model;
import io.github.biezhi.anima.annotation.Ignore;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * @author biezhi
 * @date 2018/6/7
 */
@Data
public class RpcServer extends Model {

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

    @Ignore
    private Set<String> services;

}

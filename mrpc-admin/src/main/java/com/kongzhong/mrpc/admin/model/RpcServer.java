package com.kongzhong.mrpc.admin.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.biezhi.anima.Model;
import io.github.biezhi.anima.annotation.Ignore;
import lombok.Data;
import lombok.EqualsAndHashCode;

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

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime onlineTime;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime offlineTime;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime updatedTime;

    @Ignore
    private Set<String> services;

}

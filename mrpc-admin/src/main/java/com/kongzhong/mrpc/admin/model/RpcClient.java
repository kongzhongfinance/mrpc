package com.kongzhong.mrpc.admin.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.biezhi.anima.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author biezhi
 * @date 2018/6/7
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RpcClient extends Model {

    private Long          id;
    private String        appId;
    private String        appAlias;
    private String        host;
    private Integer       pid;
    private String        owner;
    private String        ownerEmail;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime onlineTime;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createdTime;

}

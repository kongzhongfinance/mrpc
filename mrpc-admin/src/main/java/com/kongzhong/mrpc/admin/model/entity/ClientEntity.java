package com.kongzhong.mrpc.admin.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 客户端配置
 *
 * @author biezhi
 * @date 2017/7/24
 */
@Data
@Entity
@Table(name = "mrpc_client")
public class ClientEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long   id;
    private String appName;
    private String appId;
    private String haStrategy;
    private String lbStrategy;

    /**
     * 客户端服务调用超时，单位/毫秒
     */
    private int waitTimeout;

    /**
     * 快速失效重试次数
     */
    private int failOverRetry;

    /**
     * 重试间隔，单位/毫秒 默认每3秒重连一次
     */
    private int retryInterval;

    /**
     * 重试次数，默认10次
     */
    private int retryCount;

    /**
     * 客户端定时ping服务端的频率，单位/毫秒
     */
    private int pingInterval;

}

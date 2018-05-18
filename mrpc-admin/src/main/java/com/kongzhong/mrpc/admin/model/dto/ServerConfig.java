package com.kongzhong.mrpc.admin.model.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ServerConfig {

    // 业务线程池数
    private int businessThreadPoolSize;

}
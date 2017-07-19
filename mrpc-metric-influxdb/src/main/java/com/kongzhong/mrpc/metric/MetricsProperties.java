package com.kongzhong.mrpc.metric;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * metrics properties
 *
 * @author biezhi
 *         2017/6/12
 */
@ConfigurationProperties("metrics")
@Data
@ToString
public class MetricsProperties {

    // metric appId，用于标识项目
    private String appId;
    // 监控粒度，默认为类级别，可修改为method级别
    private String particle = ParticleLevel.CLASS.name();
    // influxdb配置
    private String url;
    private String username;
    private String password;
    private String database;
    private int    actions         = 1000;
    private int    flushDuration   = 100;
    private String retentionPolicy = "autogen";

}

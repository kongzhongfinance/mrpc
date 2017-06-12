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

    // 暂无卵用
    private String name;

    // influxdb配置
    private InfluxdbProperties influxdb;

}

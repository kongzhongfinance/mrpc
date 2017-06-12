package com.kongzhong.mrpc.metric;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * influxdb properties
 *
 * @author biezhi
 *         2017/6/12
 */
@ConfigurationProperties("metrics")
@Data
@ToString
public class MetricsProperties {

    private String appId;
    private String name;

    private InfluxdbProperties influxdb;

}

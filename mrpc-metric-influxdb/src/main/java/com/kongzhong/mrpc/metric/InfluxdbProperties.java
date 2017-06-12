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
@ConfigurationProperties("influxdb")
@Data
@ToString
public class InfluxdbProperties {

    private String url;
    private String username;
    private String password;
    private String database;
}

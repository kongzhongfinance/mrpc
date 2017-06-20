package com.kongzhong.mrpc.metric;

import lombok.Data;
import lombok.ToString;

/**
 * influxdb properties
 *
 * @author biezhi
 *         2017/6/12
 */
@Data
@ToString(exclude = "password")
public class InfluxdbProperties {

    private String url;
    private String username;
    private String password;
    private String database;
    private int actions = 1000;
    private int flushDuration = 100;
    private String retentionPolicy = "autogen";

}

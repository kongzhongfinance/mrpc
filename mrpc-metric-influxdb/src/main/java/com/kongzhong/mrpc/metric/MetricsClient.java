package com.kongzhong.mrpc.metric;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * metrics客户端
 */
@Slf4j
@Data
public class MetricsClient {

    private static InfluxDB influxDB;
    private String appId;
    private String name = "";
    private InfluxdbProperties influxdbProperties;

    public MetricsClient() {
    }

    public MetricsClient(MetricsProperties metricsProperties) {
        this.appId = metricsProperties.getAppId();
        this.name = metricsProperties.getName();
        this.influxdbProperties = metricsProperties.getInfluxdb();
    }

    public MetricsClient(String appId, String url, String username, String password, String database) {
        this.appId = appId;

        InfluxdbProperties influxdbProperties = new InfluxdbProperties();
        influxdbProperties.setUrl(url);
        influxdbProperties.setUsername(username);
        influxdbProperties.setPassword(password);
        influxdbProperties.setDatabase(database);
        this.influxdbProperties = influxdbProperties;
    }

    public void init() {
        if (StringUtils.isEmpty(appId))
            throw new RuntimeException("请在配置文件中设置metrics.appId");
        if (StringUtils.isEmpty(influxdbProperties.getUrl()))
            throw new RuntimeException("请在配置文件中设置metrics.influxdb.url为InfluxDb地址");
        if (StringUtils.isEmpty(influxdbProperties.getUsername()))
            throw new RuntimeException("请在配置文件中设置metrics.influxdb.username为InfluxDb用户名");
        if (StringUtils.isEmpty(influxdbProperties.getPassword()))
            throw new RuntimeException("请在配置文件中设置metrics.influxdb.password为InfluxDb密码");
        if (StringUtils.isEmpty(influxdbProperties.getDatabase()))
            throw new RuntimeException("请在配置文件中设置metrics.influxdb.database为InfluxDb数据库名");

        influxDB = InfluxDBFactory.connect(influxdbProperties.getUrl(), influxdbProperties.getUsername(), influxdbProperties.getPassword());
        influxDB.createDatabase(influxdbProperties.getDatabase());
        influxDB.enableBatch(influxdbProperties.getActions(), influxdbProperties.getFlushDuration(), TimeUnit.MILLISECONDS);
    }

    public void dispose() {
        influxDB.close();
    }

    public void write(String measurement, long count, long time, Map<String, String> tags) {
        if (tags == null)
            tags = new HashMap<>();
        try {
            Point point = Point.measurement(measurement)
                    .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    .addField("during", time)
                    .addField("count", count)
                    .tag(tags)
                    .build();
            influxDB.write(influxdbProperties.getDatabase(), "autogen", point);
        } catch (Exception ex) {
            log.error("打点到InfluxDb出现异常", ex);
        }
    }
}

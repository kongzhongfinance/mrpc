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

@Slf4j
@Data
public class MetricsClient {

    private static InfluxDB influxDB;
    private String url;
    private String username;
    private String password;
    private String database;

    public MetricsClient(InfluxdbProperties influxdbProperties) {
        this.url = influxdbProperties.getUrl();
        this.username = influxdbProperties.getUsername();
        this.password = influxdbProperties.getPassword();
        this.database = influxdbProperties.getDatabase();
    }

    public MetricsClient(String url, String username, String password, String database) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.database = database;
    }

    public void init() {
        if (StringUtils.isEmpty(url))
            throw new RuntimeException("请在配置文件中设置metrics.url为InfluxDb地址");
        if (StringUtils.isEmpty(username))
            throw new RuntimeException("请在配置文件中设置metrics.username为InfluxDb用户名");
        if (StringUtils.isEmpty(password))
            throw new RuntimeException("请在配置文件中设置metrics.password为InfluxDb密码");
        if (StringUtils.isEmpty(database))
            throw new RuntimeException("请在配置文件中设置metrics.database为InfluxDb数据库名");
        influxDB = InfluxDBFactory.connect(url, username, password);
        influxDB.createDatabase(database);
        influxDB.enableBatch(1000, 100, TimeUnit.MILLISECONDS);
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
            influxDB.write(database, "autogen", point);
        } catch (Exception ex) {
            log.error("打点到InfluxDb出现异常", ex);
        }
    }
}

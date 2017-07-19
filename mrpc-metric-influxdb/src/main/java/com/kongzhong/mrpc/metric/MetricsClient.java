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

    private static final String UNKNOWN_HOST = "(unknown)";
    private InfluxDB          influxDB;
    private String            appId;
    private MetricsProperties metricsProperties;

    MetricsClient(MetricsProperties metricsProperties) {
        this.metricsProperties = metricsProperties;
        this.appId = System.getProperty("APPID");
        if (null == this.appId) {
            this.appId = metricsProperties.getAppId();
        }
        if (null == this.appId) {
            this.appId = UNKNOWN_HOST;
        }
        this.init();
    }

    void init() {
        if (StringUtils.isEmpty(appId))
            throw new RuntimeException("请在配置文件中设置metrics.appId");
        if (StringUtils.isEmpty(metricsProperties.getUrl()))
            throw new RuntimeException("请在配置文件中设置metrics.influxdb.url为InfluxDb地址");
        if (StringUtils.isEmpty(metricsProperties.getUsername()))
            throw new RuntimeException("请在配置文件中设置metrics.influxdb.username为InfluxDb用户名");
        if (StringUtils.isEmpty(metricsProperties.getPassword()))
            throw new RuntimeException("请在配置文件中设置metrics.influxdb.password为InfluxDb密码");
        if (StringUtils.isEmpty(metricsProperties.getDatabase()))
            throw new RuntimeException("请在配置文件中设置metrics.influxdb.database为InfluxDb数据库名");

        influxDB = InfluxDBFactory.connect(metricsProperties.getUrl(), metricsProperties.getUsername(), metricsProperties.getPassword());

        long c = influxDB.describeDatabases().stream().filter(db -> db.equals(metricsProperties.getDatabase())).count();
        if (c == 0) {
            influxDB.createDatabase(metricsProperties.getDatabase());
        }
        influxDB.enableBatch(metricsProperties.getActions(), metricsProperties.getFlushDuration(), TimeUnit.MILLISECONDS);
    }

    public void dispose() {
        influxDB.close();
    }

    void write(String measurement, long count, long time, Map<String, String> tags) {
        if (tags == null)
            tags = new HashMap<>();
        try {
            Point point = Point.measurement(measurement)
                    .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    .addField("during", time)
                    .addField("count", count)
                    .tag(tags)
                    .build();
            influxDB.write(metricsProperties.getDatabase(), "autogen", point);
        } catch (Exception ex) {
            log.error("打点到InfluxDb出现异常", ex);
        }
    }
}

title: Metrics打点
-----------------

`Metrics`打点是针对于RPC服务端进行监控统计的一个扩展插件，需要集成 `statsd-influxdb-grafana`。
为了方便起见，我使用`docker`安装了一个镜像，地址在[这里](https://github.com/samuelebistoletti/docker-statsd-influxdb-grafana)。

## 加入依赖

```xml
<dependency>
    <groupId>com.kongzhong.mrpc</groupId>
    <artifactId>mrpc-metric-influxdb</artifactId>
    <version>${mrpc-metric.version}</version>
</dependency>
```

## 服务端配置

**SpringBoot**

```bash
metrics.url=http://127.0.0.1:8086
metrics.username=telegraf
metrics.password=telegraf
metrics.database=appMetrics
# 不配置的时候会读取环境变量:APPID
metrics.appId=helloworld
# 默认打点在服务级别，可调整为方法级别
metrics.particle=method
```

## 打点预览

<img src="https://i.loli.net/2017/07/19/596ed3d1c1a79.png
" width="500" height="300"/>
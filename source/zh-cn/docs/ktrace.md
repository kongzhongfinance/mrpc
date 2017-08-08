title: KTrace跟踪
---------------

`ktrace` 是为了解决rpc服务在分布式环境下的调用链问题，目前还不完善，已经出了一个简单的版本。

## 加入依赖

`RPC` 服务端和客户端都要加入

```xml
<dependency>
    <groupId>com.kongzhong.mrpc</groupId>
    <artifactId>mrpc-ktrace</artifactId>
    <version>${mrpc-ktrace.version}</version>
</dependency>
```

## Web接入

服务的入口是从某个web请求开始的，你可以在Web端添加一个`Filter`来贯穿整个请求链。

> 当然别忘了添加上面的依赖，因为这个Filter是在那个库里面

**SpringBoot Web环境配置**

```java
/**
 * RPC服务同时是Web服务的需要注入
 */
@Bean
public FilterRegistrationBean traceFilter() {
    ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean();
    servletRegistrationBean.setName("TraceFilter");
    FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new TraceFilter(), servletRegistrationBean);
    filterRegistrationBean.setName("TraceFilter");
    filterRegistrationBean.setUrlPatterns(Collections.singletonList("/*"));
    return filterRegistrationBean;
}
```

**web.xml配置**

```xml
<filter>
    <filter-name>TraceFilter</filter-name>
    <filter-class>com.kongzhong.mrpc.ktrace.interceptor.TraceFilter</filter-class>
</filter>
<filter-mapping>
    <filter-name>TraceFilter</filter-name>
    <url-pattern>/*</url-pattern>
    <dispatcher>REQUEST</dispatcher>
</filter-mapping>
```

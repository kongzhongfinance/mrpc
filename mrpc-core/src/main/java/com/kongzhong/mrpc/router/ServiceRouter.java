package com.kongzhong.mrpc.router;

import com.google.common.collect.Maps;
import com.kongzhong.mrpc.model.ServiceMeta;

import java.util.Map;

/**
 * 服务路由映射表
 *
 * @author biezhi
 *         2017/4/21
 */
public class ServiceRouter {

    private Map<String, ServiceMeta> routes = Maps.newConcurrentMap();

    public void add(String key, ServiceMeta serviceMeta) {
        routes.put(key, serviceMeta);
    }

    public ServiceMeta get(String key) {
        return routes.get(key);
    }

    public boolean contains(String key) {
        return routes.containsKey(key);
    }
}

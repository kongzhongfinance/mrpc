package com.kongzhong.mrpc.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kongzhong.mrpc.interceptor.RpcInteceptor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * RPC映射关系存储
 *
 * @author biezhi
 *         2017/4/24
 */
@Data
@NoArgsConstructor
public class RpcMapping {

    private Map<String, Object> handlerMap = Maps.newConcurrentMap();

    private List<RpcInteceptor> inteceptors = Lists.newArrayList();

    private static final class RpcMappingHolder {
        private static final RpcMapping $ = new RpcMapping();
    }

    public void addHandler(String key, Object value) {
        handlerMap.put(key, value);
    }

    public void addInterceptors(List<RpcInteceptor> inteceptors) {
        if (null != inteceptors && !inteceptors.isEmpty()) {
            this.inteceptors.addAll(inteceptors);
        }
    }

    public static RpcMapping me() {
        return RpcMappingHolder.$;
    }

}

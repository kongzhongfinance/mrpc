package com.kongzhong.mrpc.event;

import com.google.common.collect.Maps;
import com.kongzhong.mrpc.model.RpcContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 事件
 * <p>
 * Created by biezhi on 03/07/2017.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Event {

    private RpcContext rpcContext;

    @Builder.Default
    private Map<String, Object> attribute = Maps.newHashMap();

    public Object getAttribute(String key) {
        return attribute.get(key);
    }

    public void setAttribute(String key, Object value) {
        attribute.put(key, value);
    }

}

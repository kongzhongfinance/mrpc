package com.kongzhong.mrpc.serialize;

import com.alibaba.fastjson.JSON;

/**
 * @author biezhi
 *         2017/5/11
 */
public class FastJsonSerialize implements JSONSerialize {

    @Override
    public String toJSONString(Object object) {
        return JSON.toJSONString(object);
    }

    @Override
    public <T> T parseObject(String json, Class<T> type) {
        return JSON.parseObject(json, type);
    }

}

package com.kongzhong.mrpc.utils;

import com.alibaba.fastjson.JSON;
import com.kongzhong.mrpc.enums.JSONEnum;
import com.kongzhong.mrpc.serialize.FastJsonSerialize;
import com.kongzhong.mrpc.serialize.JSONSerialize;
import org.springframework.util.Assert;

/**
 * @author biezhi
 *         2017/4/20
 */
public class JSONUtils {

    private static JSONSerialize jsonSerialize = new FastJsonSerialize();

    public static void setJSONImpl(JSONEnum jsonImpl) {
        Assert.notNull(jsonImpl);
        if (jsonImpl == JSONEnum.FASTJSON) {
            jsonSerialize = new FastJsonSerialize();
        }
        if (jsonImpl == JSONEnum.JACKSON) {
            jsonSerialize = new FastJsonSerialize();
        }
        if (jsonImpl == JSONEnum.GSON) {
            jsonSerialize = new FastJsonSerialize();
        }
    }

    public static String toJSONString(Object object) {
        return jsonSerialize.toJSONString(object);
    }

    public static <T> T parseObject(String json, Class<T> type) {
        return jsonSerialize.parseObject(json, type);
    }

}
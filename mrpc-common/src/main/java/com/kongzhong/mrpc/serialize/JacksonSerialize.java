package com.kongzhong.mrpc.serialize;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kongzhong.mrpc.exception.SerializeException;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;

/**
 * Jackson JSON序列化实现
 *
 * @author biezhi
 *         2017/5/11
 */
@Slf4j
public class JacksonSerialize implements JSONSerialize {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        /**
         * 默认非空不输出，时间格式
         */
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Java对象转JSON字符串
     *
     * @param object
     * @return
     */
    @Override
    public String toJSONString(Object object) {
        String jsonStr;
        try {
            jsonStr = objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("Java convert JSON error", e);
            throw new SerializeException(e);
        }
        return jsonStr;
    }

    /**
     * json字符串转Java对象
     *
     * @param json
     * @param type
     * @param <T>
     * @return
     */
    @Override
    public <T> T parseObject(String json, Class<T> type) {
        T obj;
        try {
            obj = objectMapper.readValue(json, type);
        } catch (Exception e) {
            log.error("JSON convert Java error", e);
            throw new SerializeException(e);
        }
        return obj;
    }

}

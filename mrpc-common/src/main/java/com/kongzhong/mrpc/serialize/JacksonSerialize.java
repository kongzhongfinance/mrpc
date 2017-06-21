package com.kongzhong.mrpc.serialize;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kongzhong.mrpc.exception.SerializeException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
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
        // 默认非空不输出，时间格式
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Java对象转JSON字符串
     *
     * @param object
     * @return
     */
    @Override
    public String toJSONString(Object object) throws SerializeException {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("Object to json stirng error", e);
            throw new SerializeException(e);
        }
    }

    @Override
    public String toJSONString(Object object, boolean pretty) throws SerializeException {
        if (!pretty) {
            return toJSONString(object);
        }
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (Exception e) {
            log.error("Object to json stirng error", e);
            throw new SerializeException(e);
        }
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
    public <T> T parseObject(String json, Class<T> type) throws SerializeException {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            log.error("Json parse to object error", e);
            throw new SerializeException(e);
        }
    }

    /**
     * json转obj
     */
    @Override
    public <T> T parseObject(String json, Type type) {
        try {
            return objectMapper.readValue(json, genJavaType(type));
        } catch (Exception e) {
            log.error("Json parse to object error", e);
        }
        return null;
    }

    private JavaType genJavaType(Type type) {
        return objectMapper.getTypeFactory().constructType(type);
    }
}

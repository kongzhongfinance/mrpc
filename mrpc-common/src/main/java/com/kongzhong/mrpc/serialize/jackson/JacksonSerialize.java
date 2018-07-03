package com.kongzhong.mrpc.serialize.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.kongzhong.mrpc.exception.SerializeException;
import com.kongzhong.mrpc.exception.SystemException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.zapodot.jackson.java8.JavaOptionalModule;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

/**
 * JSON工具类
 *
 * @author biezhi
 * 2017/4/20
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JacksonSerialize {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.registerModule(initModule());
        MAPPER.registerModule(new JavaOptionalModule());
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private static SimpleModule initModule() {
        return new SimpleModule().
                addDeserializer(Date.class, new DateDeserialize()).
                addSerializer(LocalTime.class, new LocalTimeSerializer()).
                addDeserializer(LocalTime.class, new LocalTimeDeserializer()).
                addSerializer(LocalDate.class, new LocalDateSerializer()).
                addDeserializer(LocalDate.class, new LocalDateDeserializer()).
                addSerializer(LocalDateTime.class, new LocalDateTimeSerializer()).
                addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
    }

    public static JavaType getJavaType(Type type) {
        return MAPPER.getTypeFactory().constructType(type);
    }

    /**
     * Java对象转JSON字符串
     *
     * @param object
     * @return
     */
    public static String toJSONString(Object object) throws SerializeException {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            throw new SerializeException(e);
        }
    }

    public static String toJSONString(Object object, boolean pretty) throws SerializeException {
        if (!pretty) {
            return toJSONString(object);
        }
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (Exception e) {
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
    public static <T> T parseObject(String json, Class<T> type) throws SerializeException {
        try {
            return MAPPER.readValue(json, type);
        } catch (Exception e) {
            log.error("Json parse to object error", e);
            throw new SerializeException(e);
        }
    }

    /**
     * json转obj
     */
    public static <T> T parseObject(String json, Type type) {
        try {
            return MAPPER.readValue(json, getJavaType(type));
        } catch (Exception e) {
            log.error("Json parse to object error", e);
        }
        return null;
    }

    public static <T> T parseObject(String json, TypeReference<T> typeReference) {
        try {
            return MAPPER.readValue(json, typeReference);
        } catch (Exception e) {
            log.error("Json parse to object error", e);
            return null;
        }
    }

}
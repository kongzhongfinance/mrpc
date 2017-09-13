package com.kongzhong.mrpc.serialize.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author biezhi
 * @date 2017/9/13
 */
@Slf4j
public class DateDeserialize extends JsonDeserializer<Date> {

    @Override
    public Date deserialize(JsonParser jsonparser, DeserializationContext ctxt) throws IOException {
        String        dateStr   = jsonparser.getText();
        LocalDateTime formatted = LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Instant       instant   = formatted.atZone(ZoneId.systemDefault()).toInstant();
        Date          date      = Date.from(instant);
        return date;
    }
}

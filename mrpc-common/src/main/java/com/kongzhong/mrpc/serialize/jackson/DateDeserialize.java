package com.kongzhong.mrpc.serialize.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.kongzhong.mrpc.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static com.kongzhong.mrpc.utils.StringUtils.isLong;

/**
 * String -> Date
 *
 * @author biezhi
 * @date 2017/9/13
 */
@Slf4j
public class DateDeserialize extends JsonDeserializer<Date> {

    private static final DateTimeFormatter DTF     = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter NEW_DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public Date deserialize(JsonParser jsonparser, DeserializationContext ctxt) throws IOException {
        String dateStr = jsonparser.getText();
        if (StringUtils.isEmpty(dateStr)) {
            return null;
        }
        if (isLong(dateStr)) {
            return new Date(Long.valueOf(dateStr));
        }

        LocalDateTime formatted = null;
        if (dateStr.contains(".")) {
            formatted = LocalDateTime.parse(dateStr, NEW_DTF);
        } else {
            formatted = LocalDateTime.parse(dateStr, DTF);
        }

        Instant instant = formatted.atZone(ZoneId.systemDefault()).toInstant();
        Date    date    = Date.from(instant);
        return date;
    }

}

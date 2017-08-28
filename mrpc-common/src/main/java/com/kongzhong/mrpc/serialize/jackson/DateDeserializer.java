package com.kongzhong.mrpc.serialize.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateDeserializer extends JsonDeserializer<Date> {

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        String dateString = jsonParser.getValueAsString();
        if (null == dateString || dateString.isEmpty()) {
            return null;
        }
        Instant instant = LocalDate.parse(dateString, dtf).atStartOfDay()
                .atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }
}

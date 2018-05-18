package com.kongzhong.mrpc.serialize.jackson;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class LocalTimeDeserializer extends JsonDeserializer<LocalTime> {
    @Override
    public LocalTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        String dateString = ((JsonNode) jp.getCodec().readTree(jp)).asText();
        return LocalTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_TIME);
    }
}

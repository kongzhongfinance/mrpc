package com.kongzhong.mrpc.serialize.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Date;

/**
 * Date -> String
 *
 * @author biezhi
 * @date 2017/9/13
 */
@Slf4j
@NoArgsConstructor
public class DateSerializer extends JsonSerializer<Date> {

    @Override
    public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        if (null == date) {
            jsonGenerator.writeString("");
            return;
        }
        jsonGenerator.writeString(date.getTime() + "");
    }

}

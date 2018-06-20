package com.kongzhong.mrpc.serialize.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.SimpleDateFormat;
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

    private static final SimpleDateFormat SDF     = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat NEW_SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        String time = date.getTime() + "";
        if (time.endsWith("000")) {
            jsonGenerator.writeString(SDF.format(date) + ".000");
        } else {
            jsonGenerator.writeString(NEW_SDF.format(date));
        }
    }

}

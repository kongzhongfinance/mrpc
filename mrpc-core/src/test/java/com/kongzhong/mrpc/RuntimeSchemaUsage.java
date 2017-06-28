package com.kongzhong.mrpc;

import com.kongzhong.mrpc.model.Bar;
import com.kongzhong.mrpc.model.Foo;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Kostiantyn Shchepanovskyi
 */
public class RuntimeSchemaUsage {

    private final LinkedBuffer BUFFER = LinkedBuffer.allocate();
    private final Schema<Foo> SCHEMA = RuntimeSchema.getSchema(Foo.class);

    @Test
    public void serializeAndDeserialize() throws Exception {
        Foo foo = null;
        byte[] bytes = serialize(foo);
        Foo x = deserialize(bytes);
        Assert.assertEquals(foo, x);
    }

    private byte[] serialize(Foo foo) throws java.io.IOException {
        ByteArrayOutputStream temp = new ByteArrayOutputStream();
        ProtobufIOUtil.writeTo(temp, foo, SCHEMA, BUFFER);
        return temp.toByteArray();
    }

    private Foo deserialize(byte[] bytes) {
        Foo tmp = SCHEMA.newMessage();
        ProtobufIOUtil.mergeFrom(bytes, tmp, SCHEMA);
        return tmp;
    }


}
package com.kongzhong.mrpc.serialize;

import com.kongzhong.mrpc.exception.SerializeException;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * FST序列化实现
 *
 * @author biezhi
 *         2017/4/26
 */
public class FstSerialize implements RpcSerialize {

    public static final int BUF = 1024;

    @Override
    public <T> byte[] serialize(T obj) throws Exception {
        if (obj == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream(BUF);
        FSTObjectOutput out = null;
        try {
            out = new FSTObjectOutput(baos);
            out.writeObject(obj);
        } catch (IOException ex) {
            throw new SerializeException("FST: Failed to serialize object of type: " + obj.getClass(), ex);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
            }
        }
        return baos.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {
        if (data == null) {
            return null;
        }
        FSTObjectInput in = null;
        try {
            in = new FSTObjectInput(new ByteArrayInputStream(data));
            return clazz.cast(in.readObject());
        } catch (IOException ex) {
            throw new SerializeException("FST: Failed to deserialize object", ex);
        } catch (ClassNotFoundException ex) {
            throw new SerializeException("FST: Failed to deserialize object type", ex);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
            }
        }
    }

}

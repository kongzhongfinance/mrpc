package com.kongzhong.mrpc.codec;

import com.kongzhong.mrpc.serialize.RpcSerialize;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcEncoder extends MessageToByteEncoder {

    private RpcSerialize rpcSerialize;
    private Class<?> genericClass;

    public RpcEncoder(RpcSerialize rpcSerialize, Class<?> genericClass) {
        this.rpcSerialize = rpcSerialize;
        this.genericClass = genericClass;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
        if (genericClass.isInstance(in)) {
            byte[] data = rpcSerialize.serialize(in);
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }
}

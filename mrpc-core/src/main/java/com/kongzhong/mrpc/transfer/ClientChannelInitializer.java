package com.kongzhong.mrpc.transfer;

import com.kongzhong.mrpc.codec.RpcEncoder;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import com.kongzhong.mrpc.codec.RpcDecoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @author biezhi
 *         2017/4/19
 */
public class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    private RpcSerialize rpcSerialize;

    public ClientChannelInitializer(RpcSerialize rpcSerialize) {
        this.rpcSerialize = rpcSerialize;
    }

    @Override
    protected void initChannel(SocketChannel sc) throws Exception {
        sc.pipeline()
                .addLast(new RpcEncoder(rpcSerialize, RpcRequest.class))
//                .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
                .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, RpcSerialize.MESSAGE_LENGTH, 0, 0))
                .addLast(new RpcDecoder(rpcSerialize, RpcResponse.class))
                .addLast(new RpcClientHandler());
    }
}

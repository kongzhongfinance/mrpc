package com.kongzhong.mrpc.transport.tcp;

import com.kongzhong.mrpc.codec.RpcEncoder;
import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.model.RpcResponse;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import com.kongzhong.mrpc.codec.RpcDecoder;
import com.kongzhong.mrpc.transport.RpcServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.util.Map;

public class TcpServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private Map<String, Object> handlerMap;

    private RpcSerialize rpcSerialize;

    public TcpServerChannelInitializer(Map<String, Object> handlerMap, RpcSerialize rpcSerialize) {
        this.handlerMap = handlerMap;
        this.rpcSerialize = rpcSerialize;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
                .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, RpcSerialize.MESSAGE_LENGTH, 0, 0))
                .addLast(new RpcDecoder(rpcSerialize, RpcRequest.class))
                .addLast(new RpcEncoder(rpcSerialize, RpcResponse.class))
                .addLast(new RpcServerHandler(handlerMap));
    }
}

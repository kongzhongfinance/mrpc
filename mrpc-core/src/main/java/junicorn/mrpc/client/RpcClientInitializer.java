package junicorn.mrpc.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import junicorn.mrpc.MRpcContext;
import junicorn.mrpc.codec.RpcDecoder;
import junicorn.mrpc.codec.RpcEncoder;
import junicorn.mrpc.common.model.RpcRequest;
import junicorn.mrpc.common.model.RpcResponse;
import junicorn.mrpc.serialize.RpcSerialize;

public class RpcClientInitializer extends ChannelInitializer<SocketChannel> {

    private RpcSerialize rpcSerialize;

    public RpcClientInitializer(RpcSerialize rpcSerialize){
        this.rpcSerialize = rpcSerialize;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline cp = socketChannel.pipeline();
        cp.addLast(new RpcEncoder(rpcSerialize, RpcRequest.class));
        cp.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
        cp.addLast(new RpcDecoder(rpcSerialize, RpcResponse.class));
        cp.addLast(new RpcClientHandler());
    }
}

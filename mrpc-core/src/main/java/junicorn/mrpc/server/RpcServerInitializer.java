package junicorn.mrpc.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import junicorn.mrpc.codec.RpcDecoder;
import junicorn.mrpc.codec.RpcEncoder;
import junicorn.mrpc.common.model.RpcRequest;
import junicorn.mrpc.common.model.RpcResponse;
import junicorn.mrpc.inteceptor.RpcInteceptor;

import java.util.List;
import java.util.Map;

public class RpcServerInitializer extends ChannelInitializer<SocketChannel> {

    private Map<String, Object> rpcServiceMap;

    private List<RpcInteceptor> inteceptorList;

    public RpcServerInitializer(Map<String, Object> rpcServiceMap, List<RpcInteceptor> inteceptorList){
        this.rpcServiceMap = rpcServiceMap;
        this.inteceptorList = inteceptorList;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new LengthFieldBasedFrameDecoder(65536,0,4,0,0));
        pipeline.addLast(new RpcDecoder(RpcServer.rpcSerialize, RpcRequest.class));
        pipeline.addLast(new RpcEncoder(RpcServer.rpcSerialize, RpcResponse.class));
        pipeline.addLast(new RpcServerHandler(rpcServiceMap, inteceptorList));
    }
}
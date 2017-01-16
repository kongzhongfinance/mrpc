package junicorn.mrpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.channel.ChannelOption.SO_KEEPALIVE;
import static io.netty.channel.ChannelOption.TCP_NODELAY;

public class NettyClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyClient.class);

    protected Bootstrap b;
    protected EventLoopGroup group;

    public NettyClient() {
        try {
            init();
        } catch (Exception e){
        }
    }

    private void init() {
        group = new NioEventLoopGroup(4);
        b = new Bootstrap();
        b.group(group)
        .option(TCP_NODELAY, true)
        .option(SO_KEEPALIVE, true)
        .channel(NioSocketChannel.class)
        .handler(new RpcClientInitializer(RpcClient.rpcSerialize));
    }

    public ChannelFuture connect(String host, int port) {
        ChannelFuture connect = b.connect(host, port);
        connect.awaitUninterruptibly();
        return connect;
    }

    public void stop(){
        group.shutdownGracefully();
    }
}
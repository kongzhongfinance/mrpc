package com.kongzhong.mrpc.transport;

import com.kongzhong.mrpc.support.Connections;
import com.kongzhong.mrpc.config.ClientConfig;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import com.kongzhong.mrpc.transport.http.HttpClientChannelInitializer;
import com.kongzhong.mrpc.transport.http.HttpClientHandler;
import com.kongzhong.mrpc.transport.tcp.TcpClientChannelInitializer;
import com.kongzhong.mrpc.transport.tcp.TcpClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Callable;

/**
 * 请求回调处理
 *
 * @author biezhi
 *         2017/4/19
 */
public class SimpleRequestCallback implements Callable<Boolean> {

    public static final Logger log = LoggerFactory.getLogger(SimpleRequestCallback.class);

    protected EventLoopGroup eventLoopGroup = null;
    protected InetSocketAddress serverAddress = null;
    protected RpcSerialize rpcSerialize;
    private boolean isHttp = false;

    public SimpleRequestCallback(EventLoopGroup eventLoopGroup, InetSocketAddress serverAddress) {
        this.eventLoopGroup = eventLoopGroup;
        this.serverAddress = serverAddress;
        this.rpcSerialize = ClientConfig.me().getRpcSerialize();
        this.isHttp = ClientConfig.me().isHttp();
    }

    @Override
    public Boolean call() throws Exception {
        Bootstrap b = new Bootstrap();
        b.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true);

        if (isHttp) {
            b.handler(new HttpClientChannelInitializer());
        } else {
            b.handler(new TcpClientChannelInitializer());
        }

        // 和服务端建立连接,然后异步获取运行结果
        ChannelFuture channelFuture = b.connect(serverAddress);
        // 给结果绑定 Listener,
        channelFuture.addListener(new ChannelFutureListener() {
            /**
             * 当ChannelFuture 执行完毕之后(也就是异步IO结束之后) 会调用该函数
             * @param channelFuture
             * @throws Exception
             */
            public void operationComplete(final ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    log.debug("client connect success");
                    //和服务器连接成功后, 获取MessageSendHandler对象
                    Class<? extends SimpleClientHandler> clientHandler = isHttp ? HttpClientHandler.class : TcpClientHandler.class;
                    SimpleClientHandler handler = channelFuture.channel().pipeline().get(clientHandler);
                    Connections.me().addRpcClientHandler(handler);
                }
            }
        });
        return Boolean.TRUE;
    }

}

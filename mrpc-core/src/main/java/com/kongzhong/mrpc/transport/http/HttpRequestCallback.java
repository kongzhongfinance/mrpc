package com.kongzhong.mrpc.transport.http;

import com.kongzhong.mrpc.client.RpcServerLoader;
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
 * @author biezhi
 *         2017/4/19
 */
public class HttpRequestCallback implements Callable<Boolean> {

    public static final Logger log = LoggerFactory.getLogger(HttpRequestCallback.class);

    private EventLoopGroup eventLoopGroup = null;
    private InetSocketAddress serverAddress = null;

    public HttpRequestCallback(EventLoopGroup eventLoopGroup, InetSocketAddress serverAddress) {
        this.eventLoopGroup = eventLoopGroup;
        this.serverAddress = serverAddress;
    }

    @Override
    public Boolean call() throws Exception {
        Bootstrap b = new Bootstrap();
        b.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true);

        b.handler(new HttpClientChannelInitializer());

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
                    HttpClientHandler handler = channelFuture.channel().pipeline().get(HttpClientHandler.class);
                    RpcServerLoader.me().setRpcClientHandler(handler);
                }
            }
        });
        return Boolean.TRUE;
    }
}

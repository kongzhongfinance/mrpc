package com.kongzhong.mrpc.transport;

import com.kongzhong.mrpc.cluster.Connections;
import com.kongzhong.mrpc.config.ClientConfig;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import com.kongzhong.mrpc.transport.http.HttpClientChannelInitializer;
import com.kongzhong.mrpc.transport.http.HttpClientHandler;
import com.kongzhong.mrpc.transport.tcp.TcpClientChannelInitializer;
import com.kongzhong.mrpc.transport.tcp.TcpClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 请求回调处理
 *
 * @author biezhi
 *         2017/4/19
 */
@Slf4j
public class SimpleRequestCallback implements Callable<Boolean> {

    protected EventLoopGroup eventLoopGroup = null;
    protected SocketAddress serverAddress = null;
    protected RpcSerialize rpcSerialize;
    private boolean isHttp = false;

    private static final byte DEFAULT_TRCRY = 10;

    // 10次重连机会,超过后关闭客户端连接,成功后清零
    private AtomicInteger retries = new AtomicInteger(1);

    public SimpleRequestCallback(EventLoopGroup eventLoopGroup, SocketAddress serverAddress) {
        this.eventLoopGroup = eventLoopGroup;
        this.serverAddress = serverAddress;
        this.rpcSerialize = ClientConfig.me().getRpcSerialize();
        this.isHttp = ClientConfig.me().isHttp();
    }

    @Override
    public Boolean call() throws Exception {
        this.connectServer(new Bootstrap(), eventLoopGroup);
        return Boolean.TRUE;
    }

    private void connectServer(Bootstrap b, EventLoopGroup eventLoopGroup) {
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
        channelFuture.addListener(new ConnectionListener());
    }

    class ConnectionListener implements ChannelFutureListener {
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            if (future.isSuccess()) {
                retries.set(0);
                log.debug("Client connect success");
                //和服务器连接成功后, 获取MessageSendHandler对象
                Class<? extends SimpleClientHandler> clientHandler = isHttp ? HttpClientHandler.class : TcpClientHandler.class;
                SimpleClientHandler handler = future.channel().pipeline().get(clientHandler);
                Connections.me().addRpcClientHandler(handler);
            } else {
                // 启动重连
                log.warn("Client reconnect ({})", retries.get());
                final EventLoop loop = future.channel().eventLoop();
                loop.schedule(() -> {
                    if (retries.get() >= DEFAULT_TRCRY) {
                        future.channel().close();
                        log.warn("Client channel connect fail, closed.");
                    } else {
                        connectServer(new Bootstrap(), loop);
                        retries.incrementAndGet();
                    }
                }, 3L, TimeUnit.SECONDS);
            }
        }
    }
}

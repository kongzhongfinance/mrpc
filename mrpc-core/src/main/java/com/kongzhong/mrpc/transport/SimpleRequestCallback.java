package com.kongzhong.mrpc.transport;

import com.kongzhong.mrpc.client.cluster.Connections;
import com.kongzhong.mrpc.config.ClientConfig;
import com.kongzhong.mrpc.enums.TransportEnum;
import com.kongzhong.mrpc.transport.http.HttpClientChannelInitializer;
import com.kongzhong.mrpc.transport.http.HttpClientHandler;
import com.kongzhong.mrpc.transport.tcp.TcpClientChannelInitializer;
import com.kongzhong.mrpc.transport.tcp.TcpClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
    private boolean isHttp = false;

    private Lock lock = new ReentrantLock();

    private static final byte DEFAULT_TRCRY = 10;

    // 10次重连机会,超过后关闭客户端连接,成功后清零
    private AtomicInteger retries = new AtomicInteger(1);

    /**
     * 客户端服务引用
     */
    private Set<String> referNames;

    public SimpleRequestCallback(Set<String> referNames, EventLoopGroup eventLoopGroup, SocketAddress serverAddress) {
        this.isHttp = ClientConfig.me().getTransport().equals(TransportEnum.HTTP);
        this.referNames = referNames;
        this.eventLoopGroup = eventLoopGroup;
        this.serverAddress = serverAddress;
    }

    @Override
    public Boolean call() throws Exception {
        this.connectServer(new Bootstrap(), eventLoopGroup);
        return Boolean.TRUE;
    }

    /**
     * 建立连接
     *
     * @param b
     * @param eventLoopGroup
     */
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

    /**
     * 连接监听器，异步连接并重试的逻辑
     */
    class ConnectionListener implements ChannelFutureListener {
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            lock.lock();
            if (future.isSuccess()) {
                retries.set(0);
                log.debug("Client connect success");
                if (null != referNames && referNames.size() > 0) {
                    //和服务器连接成功后, 获取MessageSendHandler对象
                    Class<? extends SimpleClientHandler> clientHandler = isHttp ? HttpClientHandler.class : TcpClientHandler.class;
                    SimpleClientHandler handler = future.channel().pipeline().get(clientHandler);
                    handler.setServerAddress(serverAddress.toString().substring(1));
                    referNames.forEach(referName -> {
                        Connections.me().addRpcClientHandler(referName, handler);
                    });
                }
            } else {
                // 启动重连
                log.warn("Client reconnect [{}] ({})", serverAddress, retries.get());
                final EventLoop loop = future.channel().eventLoop();
                loop.schedule(() -> {
                    if (retries.get() >= DEFAULT_TRCRY) {
                        future.channel().close();
                        Connections.me().getAliveServers().remove(serverAddress.toString().substring(1));
                        log.warn("Client channel connect fail, closed.");
                    } else {
                        connectServer(new Bootstrap(), loop);
                        retries.incrementAndGet();
                    }
                }, 3L, TimeUnit.SECONDS);
            }
            lock.unlock();
        }
    }
}

package com.kongzhong.mrpc.transport.netty;

import com.kongzhong.mrpc.config.ClientConfig;
import com.kongzhong.mrpc.config.NettyConfig;
import com.kongzhong.mrpc.enums.TransportEnum;
import com.kongzhong.mrpc.transport.http.HttpClientChannelInitializer;
import com.kongzhong.mrpc.transport.tcp.TcpClientChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.SocketUtils;
import lombok.Getter;

import java.net.SocketAddress;
import java.util.concurrent.atomic.LongAdder;

/**
 * Netty Client
 *
 * @author biezhi
 *         24/06/2017
 */
public class NettyClient {

    @Getter
    private SocketAddress serverAddress;

    @Getter
    private String address;

    @Getter
    private boolean isRunning = true;

    @Getter
    private LongAdder retryCount = new LongAdder();

    private NettyConfig nettyConfig;

    public NettyClient(NettyConfig nettyConfig, String address) {
        this.nettyConfig = nettyConfig;
        this.address = address;

        String host = address.split(":")[0];
        int port = Integer.valueOf(address.split(":")[1]);
        this.serverAddress = SocketUtils.socketAddress(host, port);

    }

    public Bootstrap createBootstrap(EventLoopGroup eventLoopGroup) {

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, nettyConfig.getConnTimeout())
                .option(ChannelOption.SO_KEEPALIVE, true);

        if (ClientConfig.me().getTransport().equals(TransportEnum.HTTP)) {
            bootstrap.handler(new HttpClientChannelInitializer(this));
        } else {
            bootstrap.handler(new TcpClientChannelInitializer(this));
        }
        // 和服务端建立连接,然后异步获取运行结果
        ChannelFuture channelFuture = bootstrap.connect(serverAddress);
        // 给结果绑定 Listener,
        channelFuture.addListener(new ConnectionListener(this));
        return bootstrap;
    }

    /**
     * 重置重试次数
     */
    public void resetRetryCount() {
        retryCount = new LongAdder();
    }

    public void shutdown() {
        isRunning = false;
    }

}

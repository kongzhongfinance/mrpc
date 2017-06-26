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
import lombok.Getter;

import java.net.SocketAddress;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author biezhi
 *         24/06/2017
 */
public class NettyClient {

    @Getter
    private SocketAddress serverAddress;

    /**
     * 客户端服务引用
     */
    @Getter
    private Set<String> referNames;

    @Getter
    private LongAdder retryCount = new LongAdder();

    @Getter
    private boolean isRunning = true;

    private NettyConfig nettyConfig;

    public NettyClient(NettyConfig nettyConfig, SocketAddress serverAddress) {
        this.nettyConfig = nettyConfig;
        this.serverAddress = serverAddress;
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

    public NettyClient referers(Set<String> referNames) {
        this.referNames = referNames;
        return this;
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

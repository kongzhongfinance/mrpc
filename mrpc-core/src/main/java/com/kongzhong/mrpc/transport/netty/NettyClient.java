package com.kongzhong.mrpc.transport.netty;

import com.kongzhong.mrpc.client.LocalServiceNodeTable;
import com.kongzhong.mrpc.config.ClientConfig;
import com.kongzhong.mrpc.config.NettyConfig;
import com.kongzhong.mrpc.enums.TransportEnum;
import com.kongzhong.mrpc.exception.ConnectException;
import com.kongzhong.mrpc.model.ServiceStatus;
import com.kongzhong.mrpc.model.ServiceStatusTable;
import com.kongzhong.mrpc.serialize.jackson.JacksonSerialize;
import com.kongzhong.mrpc.transport.http.HttpClientChannelInitializer;
import com.kongzhong.mrpc.transport.http.HttpClientHandler;
import com.kongzhong.mrpc.transport.tcp.TcpClientChannelInitializer;
import com.kongzhong.mrpc.transport.tcp.TcpClientHandler;
import com.kongzhong.mrpc.utils.HttpRequest;
import com.kongzhong.mrpc.utils.StringUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.SocketUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * Netty Client
 *
 * @author biezhi
 * 24/06/2017
 */
@Slf4j
public class NettyClient {

    @Getter
    private SocketAddress serverAddress;

    @Getter
    private String address;

    @Getter
    private int weight;

    @Getter
    private boolean isRunning = true;

    @Getter
    private LongAdder retryCount = new LongAdder();

    private NettyConfig nettyConfig;

    @Setter
    private TransportEnum transport = ClientConfig.me().getTransport();

    /**
     * Channel调度map
     */
    private static final Map<Channel, ScheduledFuture> scheduledFutureMap = new HashMap<>();

    public NettyClient(NettyConfig nettyConfig, String address) {
        this.nettyConfig = nettyConfig;
        this.address = address;
        this.weight = ServiceStatusTable.me().getServerWeight(address);

        String host = address.split(":")[0];
        int    port = Integer.parseInt(address.split(":")[1]);
        this.serverAddress = SocketUtils.socketAddress(host, port);

    }

    private Bootstrap createBootstrap(EventLoopGroup eventLoopGroup) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, nettyConfig.getConnTimeout())
                .option(ChannelOption.SO_KEEPALIVE, true);

        if (this.transport.equals(TransportEnum.HTTP)) {
            bootstrap.handler(new HttpClientChannelInitializer(this));
        } else {
            bootstrap.handler(new TcpClientChannelInitializer(this));
        }
        return bootstrap;
    }

    /**
     * 同步创建Channel
     *
     * @param eventLoopGroup
     * @return
     */
    public Channel syncCreateChannel(EventLoopGroup eventLoopGroup) {
        if (LocalServiceNodeTable.isAlive(this.getAddress())) {
            return null;
        }

        Bootstrap bootstrap = this.createBootstrap(eventLoopGroup);
        // 和服务端建立连接,然后异步获取运行结果
        try {
            Channel channel = bootstrap.connect(serverAddress).sync().channel();
            while (!channel.isActive()) {
                TimeUnit.MILLISECONDS.sleep(100);
            }

            log.info("Connect {} success.", channel);

            boolean isHttp = ClientConfig.me().getTransport().equals(TransportEnum.HTTP);

            //和服务器连接成功后, 获取MessageSendHandler对象
            Class<? extends SimpleClientHandler> clientHandler = isHttp ? HttpClientHandler.class : TcpClientHandler.class;
            SimpleClientHandler                  handler       = channel.pipeline().get(clientHandler);

            // 设置节点状态为存活状态
            LocalServiceNodeTable.setNodeAlive(handler);

            String result = getServerStatus();
            if (StringUtils.isNotEmpty(result)) {
                ServiceStatus serviceStatus = JacksonSerialize.parseObject(result, ServiceStatus.class);
                this.weight = serviceStatus.getWeight();
            }

            if (isHttp && ClientConfig.me().getPingInterval() > 0) {
                this.enabledPing(channel);
            }
            return channel;
        } catch (Exception e) {
            LocalServiceNodeTable.setNodeDead(address);
            throw new ConnectException(String.format("Connect [%s] fail", address), e);
        }
    }

    /**
     * 异步创建Channel
     *
     * @param eventLoopGroup
     */
    public void asyncCreateChannel(EventLoopGroup eventLoopGroup) {
        if (LocalServiceNodeTable.isAlive(this.getAddress())) {
            return;
        }
        Bootstrap bootstrap = this.createBootstrap(eventLoopGroup);
        // 和服务端建立连接,然后异步获取运行结果
        bootstrap.connect(serverAddress).addListener(new ConnectionListener(this));
    }

    /**
     * 开启客户端PING
     *
     * @param channel
     */
    void enabledPing(Channel channel) {
        ScheduledFuture scheduledFuture = channel.eventLoop().scheduleAtFixedRate(() -> {
            try {
                if (!channel.isActive()) {
                    cancelSchedule(channel);
                    return;
                }
                long   start  = System.currentTimeMillis();
                String result = getServerStatus();
                if (StringUtils.isNotEmpty(result)) {
                    log.debug("Rpc send ping for {} after 0ms", channel, (System.currentTimeMillis() - start));
                }
            } catch (Exception e) {
                log.warn("Rpc send ping error: {}", e.getMessage());
            }
        }, 0, ClientConfig.me().getPingInterval(), TimeUnit.MILLISECONDS);
        scheduledFutureMap.put(channel, scheduledFuture);
    }

    private String getServerStatus() {
        return HttpRequest.get("http://" + this.getAddress() + "/status")
                .connectTimeout(10_000)
                .readTimeout(5000)
                .body();
    }

    /**
     * 停止ping任务
     *
     * @param channel
     */
    public void cancelSchedule(Channel channel) {
        ScheduledFuture scheduledFuture = scheduledFutureMap.get(channel);
        if (null != scheduledFuture && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(true);
        }
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

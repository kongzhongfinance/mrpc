package com.kongzhong.mrpc.transport.netty;

import com.kongzhong.mrpc.client.cluster.Connections;
import com.kongzhong.mrpc.config.ClientConfig;
import com.kongzhong.mrpc.enums.TransportEnum;
import com.kongzhong.mrpc.transport.http.HttpClientHandler;
import com.kongzhong.mrpc.transport.tcp.TcpClientHandler;
import com.kongzhong.mrpc.utils.CollectionUtils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 连接监听器，异步连接并重试的逻辑
 */
@Slf4j
public class ConnectionListener implements ChannelFutureListener {

    private NettyClient nettyClient;

    public ConnectionListener(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {

        nettyClient.getRetryCount().add(1);

        log.info("Reconnect {}, count = {}", nettyClient.getServerAddress(), nettyClient.getRetryCount().intValue());

        if (!future.isSuccess()) {
            final EventLoop loop = future.channel().eventLoop();
            loop.schedule(() -> nettyClient.createBootstrap(loop), ClientConfig.me().getRetryInterval(), TimeUnit.MILLISECONDS);
        } else {
            log.info("Reconnect {} success.", nettyClient.getServerAddress());
            Set<String> referNames = nettyClient.getReferNames();
            boolean isHttp = ClientConfig.me().getTransport().equals(TransportEnum.HTTP);
            if (CollectionUtils.isNotEmpty(referNames)) {
                //和服务器连接成功后, 获取MessageSendHandler对象
                Class<? extends SimpleClientHandler> clientHandler = isHttp ? HttpClientHandler.class : TcpClientHandler.class;
                SimpleClientHandler handler = future.channel().pipeline().get(clientHandler);
                referNames.forEach(serviceName -> Connections.me().addRpcClientHandler(serviceName, handler));
            }
        }
    }
}
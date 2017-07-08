package com.kongzhong.mrpc.transport.netty;

import com.kongzhong.mrpc.client.LocalServiceNodeTable;
import com.kongzhong.mrpc.config.ClientConfig;
import com.kongzhong.mrpc.enums.TransportEnum;
import com.kongzhong.mrpc.transport.http.HttpClientHandler;
import com.kongzhong.mrpc.transport.tcp.TcpClientHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;
import lombok.extern.slf4j.Slf4j;

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

        if (!nettyClient.isRunning() || LocalServiceNodeTable.isAlive(nettyClient.getAddress())) {
            return;
        }

        if (!future.isSuccess()) {
            if (nettyClient.getRetryCount().intValue() >= ClientConfig.me().getRetryCount()) {
                return;
            }

            nettyClient.getRetryCount().add(1);
            log.info("Reconnect {}, count = {}", nettyClient.getServerAddress(), nettyClient.getRetryCount().intValue());
            final EventLoop loop = future.channel().eventLoop();
            loop.schedule(() -> nettyClient.asyncCreateChannel(loop), ClientConfig.me().getRetryInterval(), TimeUnit.MILLISECONDS);

        } else {

            if (!nettyClient.isRunning() || LocalServiceNodeTable.isAlive(nettyClient.getAddress())) {
                return;
            }

            log.info("Connect {} success.", future.channel());

            nettyClient.resetRetryCount();

            boolean isHttp = ClientConfig.me().getTransport().equals(TransportEnum.HTTP);

            //和服务器连接成功后, 获取MessageSendHandler对象
            Class<? extends SimpleClientHandler> clientHandler = isHttp ? HttpClientHandler.class : TcpClientHandler.class;
            SimpleClientHandler handler = future.channel().pipeline().get(clientHandler);

            // 设置节点状态为存活状态
            LocalServiceNodeTable.setNodeAlive(handler);

            if (isHttp && ClientConfig.me().getPingInterval() > 0) {
                nettyClient.enabledPing(future.channel());
            }
        }
    }

}
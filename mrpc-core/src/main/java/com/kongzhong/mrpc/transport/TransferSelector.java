package com.kongzhong.mrpc.transport;

import com.kongzhong.mrpc.exception.InitializeException;
import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import com.kongzhong.mrpc.transport.http.HttpServerChannelInitializer;
import io.netty.channel.ChannelHandler;

/**
 * 传输协议选择器
 *
 * @author biezhi
 *         2017/4/20
 */
public class TransferSelector {

    private RpcSerialize rpcSerialize;

    public TransferSelector(RpcSerialize rpcSerialize) {
        this.rpcSerialize = rpcSerialize;
    }

    /**
     * 根据传输协议获取一个服务端处理handler
     *
     * @return NettyChannelHandler
     */
    public ChannelHandler getServerChannelHandler() throws RpcException {
        if (null == rpcSerialize) {
            throw new InitializeException("rpc server serialize is null.");
        }
        return new HttpServerChannelInitializer();
    }

}

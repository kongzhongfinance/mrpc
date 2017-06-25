package com.kongzhong.mrpc.transport;

import com.kongzhong.mrpc.enums.TransportEnum;
import com.kongzhong.mrpc.exception.InitializeException;
import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import com.kongzhong.mrpc.transport.http.HttpServerChannelInitializer;
import com.kongzhong.mrpc.transport.tcp.TcpServerChannelInitializer;
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
     * @param transport
     * @return
     * @see TransportEnum
     */
    public ChannelHandler getServerChannelHandler(String transport) throws RpcException {

        TransportEnum transportEnum = TransportEnum.valueOf(transport.toUpperCase());
        if (null == transportEnum) {
            throw new InitializeException("transfer type [" + transport + "] error.");
        }

        if (transportEnum.equals(TransportEnum.TCP)) {
            return new TcpServerChannelInitializer(rpcSerialize);
        }

        if (transportEnum.equals(TransportEnum.HTTP)) {
            return new HttpServerChannelInitializer();
        }

        throw new InitializeException("transfer type is null.");
    }

}

package com.kongzhong.mrpc.transport;

import com.kongzhong.mrpc.client.RpcServerLoader;
import com.kongzhong.mrpc.enums.SerializeEnum;
import com.kongzhong.mrpc.enums.TransportEnum;
import com.kongzhong.mrpc.exception.InitializeException;
import com.kongzhong.mrpc.serialize.ProtostuffSerialize;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import com.kongzhong.mrpc.transport.http.HttpServerChannelInitializer;
import com.kongzhong.mrpc.transport.tcp.TcpServerChannelInitializer;
import io.netty.channel.ChannelHandler;

import java.util.Map;

/**
 * 传输协议选择器
 *
 * @author biezhi
 *         2017/4/20
 */
public class TransferSelector {

    private Map<String, Object> handlerMap;
    private String serialize;
    private RpcSerialize rpcSerialize;

    public TransferSelector(Map<String, Object> handlerMap, String serialize) {
        this.handlerMap = handlerMap;
        this.serialize = serialize;
    }

    /**
     * 根据传输协议获取一个服务端处理handler
     *
     * @param transport
     * @return
     * @see TransportEnum
     */
    public ChannelHandler getServerChannelHandler(String transport) {

        SerializeEnum serializeEnum = SerializeEnum.valueOf(serialize.toUpperCase());
        if (null == serializeEnum) {
            throw new InitializeException("serialize type [" + serialize + "] error.");
        }

        if (serializeEnum.equals(SerializeEnum.PROTOSTUFF)) {
            rpcSerialize = new ProtostuffSerialize();
        }

        TransportEnum transportEnum = TransportEnum.valueOf(transport.toUpperCase());
        if (null == transportEnum) {
            throw new InitializeException("transfer type [" + transport + "] error.");
        }

        if (null == rpcSerialize) {
            rpcSerialize = RpcServerLoader.me().getRpcSerialize();
        }

        if (null == rpcSerialize) {
            throw new InitializeException("rpc server serialize is null.");
        }

        if (transportEnum.equals(TransportEnum.TCP)) {
            return new TcpServerChannelInitializer(handlerMap, rpcSerialize);
        }

        if (transportEnum.equals(TransportEnum.HTTP)) {
            return new HttpServerChannelInitializer(handlerMap);
        }

        throw new InitializeException("transfer type is null.");
    }

}

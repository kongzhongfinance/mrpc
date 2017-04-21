package com.kongzhong.mrpc.transport;

import com.kongzhong.mrpc.client.RpcServerLoader;
import com.kongzhong.mrpc.enums.SerializeEnum;
import com.kongzhong.mrpc.enums.TransferEnum;
import com.kongzhong.mrpc.exception.InitializeException;
import com.kongzhong.mrpc.serialize.ProtostuffSerialize;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import com.kongzhong.mrpc.transport.http.HttpClientChannelInitializer;
import com.kongzhong.mrpc.transport.http.HttpServerChannelInitializer;
import com.kongzhong.mrpc.transport.tcp.TcpClientChannelInitializer;
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

        SerializeEnum serializeEnum = SerializeEnum.valueOf(serialize);
        if (null == serializeEnum) {
            throw new InitializeException("serialize type [" + serialize + "] error.");
        }

        if (serializeEnum.equals(SerializeEnum.PROTOSTUFF)) {
            rpcSerialize = new ProtostuffSerialize();
        }

    }

    public TransferSelector() {
    }

    public ChannelHandler getServerChannelHandler(String transfer) {

        TransferEnum transferEnum = TransferEnum.valueOf(transfer);
        if (null == transferEnum) {
            throw new InitializeException("transfer type [" + transfer + "] error.");
        }

        if (null == rpcSerialize) {
            rpcSerialize = RpcServerLoader.me().getRpcSerialize();
        }

        if (null == rpcSerialize) {
            throw new InitializeException("rpc server serialize is null.");
        }

        if (transferEnum.equals(TransferEnum.TPC)) {
            return new TcpServerChannelInitializer(handlerMap, rpcSerialize);
        }

        if (transferEnum.equals(TransferEnum.HTTP)) {
            return new HttpServerChannelInitializer(handlerMap, rpcSerialize);
        }

        throw new InitializeException("transfer type is null.");
    }

    public ChannelHandler getClientChannelHandler(String transfer) {

        TransferEnum transferEnum = TransferEnum.valueOf(transfer);
        if (null == transferEnum) {
            throw new InitializeException("transfer type [" + transfer + "] error.");
        }

        RpcSerialize rpcSerialize = RpcServerLoader.me().getRpcSerialize();

        RpcServerLoader.me().setRpcSerialize(rpcSerialize);

        if (null == rpcSerialize) {
            throw new InitializeException("rpc client serialize is null.");
        }

        if (transferEnum.equals(TransferEnum.TPC)) {
            return new TcpClientChannelInitializer(rpcSerialize);
        }

        if (transferEnum.equals(TransferEnum.HTTP)) {
            return new HttpClientChannelInitializer(rpcSerialize);
        }

        throw new InitializeException("transfer type is null.");
    }

}

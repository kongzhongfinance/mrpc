package com.kongzhong.mrpc.transfer;

import com.kongzhong.mrpc.enums.SerializeEnum;
import com.kongzhong.mrpc.enums.TransferEnum;
import com.kongzhong.mrpc.exception.InitializeException;
import com.kongzhong.mrpc.serialize.ProtostuffSerialize;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import com.kongzhong.mrpc.transfer.http.HttpClientChannelInitializer;
import com.kongzhong.mrpc.transfer.http.HttpServerChannelInitializer;
import com.kongzhong.mrpc.transfer.tcp.TcpClientChannelInitializer;
import com.kongzhong.mrpc.transfer.tcp.TcpServerChannelInitializer;
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

    public TransferSelector(Map<String, Object> handlerMap, String serialize) {
        this.handlerMap = handlerMap;
        this.serialize = serialize;
    }

    public ChannelHandler getServerChannelHandler(String transfer) {

        TransferEnum transferEnum = TransferEnum.valueOf(transfer);
        if (null == transferEnum) {
            throw new InitializeException("transfer type [" + transfer + "] error.");
        }

        if (transferEnum.equals(TransferEnum.TPC)) {
            return new TcpServerChannelInitializer(handlerMap, rpcSerialize(serialize));
        }

        if (transferEnum.equals(TransferEnum.HTTP)) {
            return new HttpServerChannelInitializer(handlerMap, rpcSerialize(serialize));
        }

        throw new InitializeException("transfer type is null.");
    }

    public ChannelHandler getClientChannelHandler(String transfer) {

        TransferEnum transferEnum = TransferEnum.valueOf(transfer);
        if (null == transferEnum) {
            throw new InitializeException("transfer type [" + transfer + "] error.");
        }

        if (transferEnum.equals(TransferEnum.TPC)) {
            return new TcpClientChannelInitializer(rpcSerialize(serialize));
        }

        if (transferEnum.equals(TransferEnum.HTTP)) {
            return new HttpClientChannelInitializer(rpcSerialize(serialize));
        }

        throw new InitializeException("transfer type is null.");
    }

    private RpcSerialize rpcSerialize(String serialize) {
        SerializeEnum serializeEnum = SerializeEnum.valueOf(serialize);
        if (null == serializeEnum) {
            throw new InitializeException("serialize type [" + serialize + "] error.");
        }

        if (serializeEnum.equals(SerializeEnum.PROTOSTUFF)) {
            return new ProtostuffSerialize();
        }

        throw new InitializeException("serialize type is null.");
    }
}

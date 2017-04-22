package com.kongzhong.mrpc.transport;

import com.kongzhong.mrpc.serialize.RpcSerialize;
import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Callable;

/**
 * @author biezhi
 *         2017/4/19
 */
public abstract class SimpleRequestCallback<T> implements Callable<T> {

    public static final Logger log = LoggerFactory.getLogger(SimpleRequestCallback.class);

    protected EventLoopGroup eventLoopGroup = null;
    protected InetSocketAddress serverAddress = null;
    protected RpcSerialize rpcSerialize;

    public SimpleRequestCallback(EventLoopGroup eventLoopGroup, InetSocketAddress serverAddress, RpcSerialize rpcSerialize) {
        this.eventLoopGroup = eventLoopGroup;
        this.serverAddress = serverAddress;
        this.rpcSerialize = rpcSerialize;
    }

    public abstract T call() throws Exception;

}

package junicorn.mrpc.connection;

import io.netty.channel.ChannelFuture;
import junicorn.mrpc.async.RpcFuture;
import junicorn.mrpc.common.config.Constant;
import junicorn.mrpc.common.exception.MRpcException;
import junicorn.mrpc.common.model.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * rpc connection
 *
 * Created by biezhi on 2016/12/9.
 */
public class Connection {

    private final static Logger LOGGER = LoggerFactory.getLogger(Connection.class);

    private ChannelFuture future;
    private AtomicBoolean isConnected = new AtomicBoolean();

    private String host;
    private int port;

    public Connection(String host, int port) {
        this.host = host;
        this.port = port;
        this.isConnected.set(false);
        this.future = null;
    }

    public RpcFuture write(final RpcRequest request) throws Exception {
        if (!isConnected()) {
            throw new MRpcException("client is not connected");
        }

        // connect time out
        int retry = Constant.RPC_RETRY_COUNT;
        while(!future.channel().isActive() && retry > 0){
            TimeUnit.SECONDS.sleep(5);
            LOGGER.warn("try to reconnect the service [{}:{}]", host, port);
            retry--;
        }

        if(!future.channel().isActive()){
            LOGGER.error("client connect is time out.");
            return null;
        }

        request.setRequestIp(future.channel().localAddress().toString());
        RpcFuture rpcFuture = new RpcFuture(request);

        ConnManager.futureMap.put(request.getRequestId(), rpcFuture);
        try {
            future.channel().writeAndFlush(request);
        } catch (Exception e) {
            ConnManager.futureMap.remove(request.getRequestId());
            LOGGER.error(e.getMessage(), e);
        }
        return rpcFuture;
    }

    public boolean isConnected() {
        return isConnected.get();
    }

    public Connection setIsConnected(boolean isConnected) {
        this.isConnected.set(isConnected);
        return this;
    }

    public ChannelFuture getFuture() {
        return future;
    }

    public Connection setFuture(ChannelFuture future) {
        this.future = future;
        return this;
    }

    public void close(){
        this.future.channel().close();
    }

}

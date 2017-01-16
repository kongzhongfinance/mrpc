package junicorn.mrpc.connection;

import com.google.common.collect.Maps;
import io.netty.channel.ChannelFuture;
import junicorn.mrpc.async.RpcFuture;
import junicorn.mrpc.client.NettyClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * rpc connection manager
 *
 * Created by biezhi on 2016/12/12.
 */
public final class ConnManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnManager.class);

    // connnections
    public static Map<String, Connection> connections = Maps.newConcurrentMap();

    public final static Map<String, RpcFuture> futureMap = Maps.newConcurrentMap();

    private ConnManager(){
    }

    public static Collection<Connection> getConns(){
        return connections.values();
    }

    /**
     * update node list
     *
     * @param addressList
     */
    public static void updateNodes(Set<String> addressList){
        if(null != addressList){
            connections.clear();
            for(String address : addressList){
                String addr = address.split(":")[0];
                int port = Integer.valueOf(address.split(":")[1]);
                NettyClient nettyClient = new NettyClient(addr, port);
                ChannelFuture channelFuture = nettyClient.connect();
                Connection connection = new Connection();
                connection.setFuture(channelFuture);
                connection.setIsConnected(true);
                connections.put(address, connection);
            }
        }
    }

    public static void remove(String address){
        connections.remove(address);
    }

    /**
     * stop rpc connection
     */
    public static void stop() {
        for(Connection connection : connections.values()){
            connection.close();
        }
    }

}

package com.kongzhong.mrpc.registry;

import com.google.common.net.HostAndPort;
import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.model.ServiceBean;
import com.orbitz.consul.Consul;

/**
 * @author biezhi
 *         29/06/2017
 */
public class ConsulServiceRegistry implements ServiceRegistry {

    private Consul consul;

    protected final static String CONSUL_NAME = "consul_node_mrpc";
    protected final static String CONSUL_ID = "consul_node_id";
    protected final static String CONSUL_TAGS = "mrpc";
    protected final static String CONSUL_HEALTH_INTERVAL = "3s";

    private final static int CONSUL_CONNECT_PERIOD = 1000;

    private final HostAndPort hostAndPort;

    public ConsulServiceRegistry(String host, int port) {
        this.hostAndPort = HostAndPort.fromString(host + ":" + port);
        this.consul = Consul.builder().withHostAndPort(this.hostAndPort).build();
    }

    @Override
    public void register(ServiceBean serviceBean) throws RpcException {


    }

    @Override
    public void unregister(ServiceBean serviceBean) throws RpcException {

    }

}

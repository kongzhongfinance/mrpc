package com.kongzhong.mrpc.server;

import com.kongzhong.mrpc.registry.ServiceRegistry;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@NoArgsConstructor
public class RpcServer extends SimpleRpcServer {

    public RpcServer(String serverAddress) {
        super(serverAddress);
    }

    public RpcServer(String serverAddress, ServiceRegistry serviceRegistry) {
        super(serverAddress, serviceRegistry);
    }

}
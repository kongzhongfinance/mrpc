package com.kongzhong.mrpc.admin.service;

import com.blade.ioc.annotation.Bean;
import com.kongzhong.mrpc.admin.model.RpcServer;

import static io.github.biezhi.anima.Anima.select;

/**
 * @author biezhi
 * @date 2018/6/7
 */
@Bean
public class ServerService {

    public void saveServer(RpcServer rpcServer) {
        RpcServer temp = select().from(RpcServer.class)
                .where(RpcServer::getAppId, rpcServer.getAppId())
                .and(RpcServer::getHost, rpcServer.getHost())
                .and(RpcServer::getPort, rpcServer.getPort())
                .one();

        if (null == temp) {
            rpcServer.save();
        } else {
            rpcServer.setId(temp.getId());
            rpcServer.update();
        }
    }

}

package com.kongzhong.mrpc.admin.service;

import com.blade.ioc.annotation.Bean;
import com.kongzhong.mrpc.admin.model.RpcServer;
import com.kongzhong.mrpc.admin.model.RpcService;

import java.util.Set;

import static io.github.biezhi.anima.Anima.delete;
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

    public void saveServices(String appId, Set<String> services) {
        delete().from(RpcService.class).where(RpcService::getAppId, appId).execute();
        for (String service : services) {
            RpcService rpcService = new RpcService();
            rpcService.setAppId(appId);
            rpcService.setServiceId(service);
            rpcService.save();
        }
    }

}

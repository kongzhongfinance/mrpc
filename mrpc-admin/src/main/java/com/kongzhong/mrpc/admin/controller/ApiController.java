package com.kongzhong.mrpc.admin.controller;

import com.blade.ioc.annotation.Inject;
import com.blade.kit.JsonKit;
import com.blade.mvc.annotation.Path;
import com.blade.mvc.annotation.PostRoute;
import com.blade.mvc.http.Request;
import com.kongzhong.mrpc.admin.model.RpcNotice;
import com.kongzhong.mrpc.admin.model.RpcServer;
import com.kongzhong.mrpc.admin.service.ServerService;
import com.kongzhong.mrpc.enums.NodeStatusEnum;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * @author biezhi
 * @date 2018/6/7
 */
@Slf4j
@Path(value = "api", restful = true)
public class ApiController {

    @Inject
    private ServerService serverService;

    @PostRoute("server")
    public void server(Request request) {
        String nodeStatus = request.header("notice_status");
        String content    = request.bodyToString();

        RpcNotice rpcNotice = new RpcNotice();
        rpcNotice.setApiType("server");
        rpcNotice.setAddress(request.header("address"));
        rpcNotice.setCreatedTime(LocalDateTime.now());
        rpcNotice.setContent(content);
        rpcNotice.save();

        log.info("接收到: [{}] - {}", nodeStatus, content);
        RpcServer rpcServer = JsonKit.formJson(content, RpcServer.class);

        // 上线则更新接口列表
        if (NodeStatusEnum.ONLINE.toString().equals(nodeStatus)) {
            serverService.saveServices(rpcServer.getAppId(), rpcServer.getServices());
        }

        serverService.saveServer(rpcServer);
    }

    @PostRoute("client")
    public void client(Request request) {
        System.out.println(request.bodyToString());
    }

}

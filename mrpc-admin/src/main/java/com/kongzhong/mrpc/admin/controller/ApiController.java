package com.kongzhong.mrpc.admin.controller;

import com.blade.ioc.annotation.Inject;
import com.blade.kit.JsonKit;
import com.blade.mvc.annotation.Path;
import com.blade.mvc.annotation.PostRoute;
import com.blade.mvc.http.Request;
import com.kongzhong.mrpc.admin.model.RpcServer;
import com.kongzhong.mrpc.admin.service.ServerService;
import com.kongzhong.mrpc.enums.NoticeType;
import lombok.extern.slf4j.Slf4j;

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
        String noticeType = request.header("notice_type");
        String content    = request.bodyToString();

        log.info("接收到: [{}] - {}", noticeType, content);

        if (NoticeType.SERVER_ONLINE.toString().equals(noticeType)) {
            RpcServer rpcServer = JsonKit.formJson(content, RpcServer.class);
            serverService.saveServer(rpcServer);
        }

    }

    @PostRoute("client")
    public void client(Request request) {
        System.out.println(request.bodyToString());
    }

}

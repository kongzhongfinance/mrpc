package com.kongzhong.mrpc.admin.controller;

import com.blade.ioc.annotation.Inject;
import com.blade.kit.JsonKit;
import com.blade.mvc.annotation.BodyParam;
import com.blade.mvc.annotation.Path;
import com.blade.mvc.annotation.PostRoute;
import com.blade.mvc.http.Request;
import com.blade.mvc.ui.RestResponse;
import com.kongzhong.mrpc.admin.model.RpcNotice;
import com.kongzhong.mrpc.admin.model.RpcServer;
import com.kongzhong.mrpc.admin.service.ClientService;
import com.kongzhong.mrpc.admin.service.ServerService;
import com.kongzhong.mrpc.enums.NodeStatusEnum;
import com.kongzhong.mrpc.model.RpcClientNotice;
import com.kongzhong.mrpc.utils.HttpRequest;
import com.kongzhong.mrpc.utils.NetUtils;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import static com.kongzhong.mrpc.admin.tasks.PingTask.DEFAULT_TIME_OUT;

/**
 * @author biezhi
 * @date 2018/6/7
 */
@Slf4j
@Path(value = "api", restful = true)
public class ApiController {

    @Inject
    private ServerService serverService;

    @Inject
    private ClientService clientService;

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
        String nodeStatus = request.header("notice_status");
        String address = request.header("address");
        String content    = request.bodyToString();

        RpcNotice rpcNotice = new RpcNotice();
        rpcNotice.setApiType("client");
        rpcNotice.setAddress(address);
        rpcNotice.setCreatedTime(LocalDateTime.now());
        rpcNotice.setContent(content);
        rpcNotice.save();

        log.info("接收到: [{}] - {}", nodeStatus, content);

        RpcClientNotice rpcClientNotice = JsonKit.formJson(content, RpcClientNotice.class);
        if (NodeStatusEnum.ONLINE.toString().equals(nodeStatus)) {
            clientService.saveClient(rpcClientNotice);
        }
        clientService.updateServerCall(rpcClientNotice);
    }

    @PostRoute("online")
    public RestResponse online(@BodyParam RpcServer rpcServer) {
        String url = "http://" + rpcServer.getHost() + ":" + rpcServer.getPort() + "/online";
        if (!NetUtils.pingHost(rpcServer.getHost(), rpcServer.getPort(), DEFAULT_TIME_OUT)) {
            return RestResponse.fail("主机 ping 不通");
        } else {
            int code = HttpRequest.get(url).connectTimeout(DEFAULT_TIME_OUT).readTimeout(DEFAULT_TIME_OUT).code();
            if (code == 200) {
                rpcServer.setStatus(NodeStatusEnum.ONLINE.toString());
                rpcServer.update();
                return RestResponse.ok();
            } else {
                return RestResponse.fail(code, "离线失败");
            }
        }
    }

    @PostRoute("offline")
    public RestResponse offline(@BodyParam RpcServer rpcServer) {
        String url = "http://" + rpcServer.getHost() + ":" + rpcServer.getPort() + "/offline";
        if (!NetUtils.pingHost(rpcServer.getHost(), rpcServer.getPort(), DEFAULT_TIME_OUT)) {
            return RestResponse.fail("主机 ping 不通");
        } else {
            int code = HttpRequest.get(url).connectTimeout(DEFAULT_TIME_OUT).readTimeout(DEFAULT_TIME_OUT).code();
            if (code == 200) {
                rpcServer.setStatus(NodeStatusEnum.OFFLINE.toString());
                rpcServer.update();
                return RestResponse.ok();
            } else {
                return RestResponse.fail(code, "离线失败");
            }
        }
    }

}

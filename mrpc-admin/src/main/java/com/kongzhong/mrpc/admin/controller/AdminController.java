package com.kongzhong.mrpc.admin.controller;

import com.blade.ioc.annotation.Inject;
import com.blade.mvc.annotation.*;
import com.blade.mvc.ui.RestResponse;
import com.kongzhong.mrpc.admin.model.RpcNotice;
import com.kongzhong.mrpc.admin.model.RpcServer;
import com.kongzhong.mrpc.admin.model.SysLog;
import com.kongzhong.mrpc.admin.params.PageParam;
import com.kongzhong.mrpc.admin.service.ServerService;
import com.kongzhong.mrpc.admin.tasks.PingTask;
import com.kongzhong.mrpc.admin.vo.ServerMap;
import io.github.biezhi.anima.Anima;
import io.github.biezhi.anima.enums.OrderBy;

import static io.github.biezhi.anima.Anima.select;

/**
 * @author biezhi
 * @date 2018/6/7
 */
@Path(value = "admin", restful = true)
public class AdminController {

    @Inject
    private ServerService serverService;

    @Inject
    private PingTask pingTask;

    @GetRoute("server/map")
    public ServerMap serverMap() {
        return serverService.getServerMap();
    }

    @GetRoute("server/list")
    public RestResponse serverList() {
        return RestResponse.ok(serverService.getServerList());
    }

    @GetRoute("server/detail/:id")
    public RestResponse serverDetail(@PathParam Long id) {
        return RestResponse.ok(serverService.getServerDetail(id));
    }

    @PostRoute("server/delete")
    public RestResponse deleteNode(@BodyParam RpcServer rpcServer) {
        pingTask.removeUrl(rpcServer.getHost() + ":" + rpcServer.getPort());
        return RestResponse.ok(Anima.deleteById(RpcServer.class, rpcServer.getId()));
    }

    @PostRoute("server/update")
    public RestResponse rename(@BodyParam RpcServer rpcServer) {
        return RestResponse.ok(rpcServer.update());
    }

    @PostRoute("notice/list")
    public RestResponse noticeList(@BodyParam PageParam pageParam) {
        return RestResponse.ok(select().from(RpcNotice.class).order(RpcNotice::getId, OrderBy.DESC)
                .page(pageParam.getPage(), pageParam.getLimit()));
    }

    @PostRoute("log/list")
    public RestResponse logList(@BodyParam PageParam pageParam) {
        return RestResponse.ok(select().from(SysLog.class).order(SysLog::getId, OrderBy.DESC)
                .page(pageParam.getPage(), pageParam.getLimit()));
    }


}

package com.kongzhong.mrpc.admin.controller;

import com.blade.ioc.annotation.Inject;
import com.blade.mvc.annotation.GetRoute;
import com.blade.mvc.annotation.Path;
import com.blade.mvc.annotation.PathParam;
import com.blade.mvc.ui.RestResponse;
import com.kongzhong.mrpc.admin.service.ServerService;
import com.kongzhong.mrpc.admin.vo.ServerDetailVO;
import com.kongzhong.mrpc.admin.vo.ServerMap;
import com.kongzhong.mrpc.admin.vo.ServerVO;

import java.util.List;

/**
 * @author biezhi
 * @date 2018/6/7
 */
@Path(value = "admin", restful = true)
public class AdminController {

    @Inject
    private ServerService serverService;

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
}

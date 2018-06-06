package com.kongzhong.mrpc.admin.controller;

import com.blade.ioc.annotation.Inject;
import com.blade.mvc.annotation.Path;
import com.blade.mvc.annotation.PostRoute;
import com.blade.mvc.ui.RestResponse;
import com.kongzhong.mrpc.admin.params.LoginParam;
import com.kongzhong.mrpc.admin.service.UserService;

/**
 * 认证接口
 *
 * @author biezhi
 * @date 2018/6/6
 */
@Path(value = "auth", restful = true)
public class AuthController {

    @Inject
    private UserService userService;

    @PostRoute("login")
    public RestResponse login(LoginParam loginParam) {
        userService.login(loginParam);
        return RestResponse.ok();
    }

}

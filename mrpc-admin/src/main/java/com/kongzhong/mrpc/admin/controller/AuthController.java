package com.kongzhong.mrpc.admin.controller;

import com.blade.ioc.annotation.Inject;
import com.blade.mvc.annotation.Path;
import com.blade.mvc.annotation.PostRoute;
import com.blade.mvc.http.Session;
import com.blade.mvc.ui.RestResponse;
import com.kongzhong.mrpc.admin.model.SysUser;
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

    public static final String SESSION_KEY = "login_user";
    public static final String COOKIE_KEY  = "MRPC_USER";

    @Inject
    private UserService userService;

    @PostRoute("login")
    public RestResponse<SysUser> login(LoginParam loginParam, Session session) {
        RestResponse restResponse = userService.login(loginParam);
        if (restResponse.isSuccess()) {
            session.attribute(SESSION_KEY, restResponse.getPayload());
        }
        return restResponse;
    }

}

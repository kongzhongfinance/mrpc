package com.kongzhong.mrpc.admin.hook;

import com.blade.ioc.annotation.Bean;
import com.blade.mvc.hook.Signature;
import com.blade.mvc.hook.WebHook;
import com.blade.mvc.http.Request;
import com.blade.mvc.http.Response;
import com.kongzhong.mrpc.admin.model.SysUser;

import static com.kongzhong.mrpc.admin.controller.AuthController.SESSION_KEY;

/**
 * @author biezhi
 * @date 2018/6/8
 */
@Bean
public class BaseHook implements WebHook {

    @Override
    public boolean before(Signature signature) {
        Request  request  = signature.request();
        Response response = signature.response();

//        String uri = request.uri();
//        if (uri.startsWith("/admin/")) {
//            SysUser sysUser = request.session().attribute(SESSION_KEY);
//            if (null == sysUser) {
//                response.redirect("/login");
//                return false;
//            }
//        }
        return true;
    }

}

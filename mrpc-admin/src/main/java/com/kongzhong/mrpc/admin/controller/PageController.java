package com.kongzhong.mrpc.admin.controller;

import com.blade.mvc.annotation.GetRoute;
import com.blade.mvc.annotation.Path;
import com.blade.mvc.annotation.PathParam;
import com.blade.mvc.http.Request;
import com.blade.mvc.http.Response;

/**
 * @author biezhi
 * @date 2018/6/6
 */
@Path
public class PageController {

    @GetRoute("login")
    public String login() {
        return "login.html";
    }

    @GetRoute("logout")
    public void logout(Request request, Response response) {
        request.session().remove("");
        response.redirect("/login");
    }

    @GetRoute("admin/:page.html")
    public String adminHome(@PathParam String page) {
        return page;
    }

}

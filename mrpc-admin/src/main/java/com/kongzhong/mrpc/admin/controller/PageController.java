package com.kongzhong.mrpc.admin.controller;

import com.blade.mvc.WebContext;
import com.blade.mvc.annotation.GetRoute;
import com.blade.mvc.annotation.Path;
import com.blade.mvc.annotation.PathParam;
import com.blade.mvc.annotation.Route;
import com.blade.mvc.http.Request;
import com.blade.mvc.http.Response;

/**
 * @author biezhi
 * @date 2018/6/6
 */
@Path
public class PageController {

    @GetRoute("/")
    public void index(Response response) {
        response.redirect("/login");
    }

    @GetRoute("login")
    public String login(Request request) {
        request.attribute("_pass_word", WebContext.blade().environment().getOrNull("login.password"));
        return "login.html";
    }

    @GetRoute("logout")
    public void logout(Request request, Response response) {
        request.session().remove(AuthController.SESSION_KEY);
        response.redirect("/login");
    }

    @Route("/status")
    public void status(Response response) {
        response.text("success");
    }

    @GetRoute("admin/:page.html")
    public String adminHome(@PathParam String page) {
        return page;
    }

    @GetRoute("admin/server/:id")
    public String serverDetail(@PathParam Long id) {
        return "detail.html";
    }

}

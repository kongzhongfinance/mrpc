package com.kongzhong.mrpc.admin.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 后台页面
 *
 * @author biezhi
 *         2017/5/14
 */
@Slf4j
@Controller
@RequestMapping("admin")
public class IndexController {

    @GetMapping
    public String index() {
        return "index";
    }

    @GetMapping("service")
    public String service() {
        return "service";
    }

    @GetMapping("server/config")
    public String serverConfig() {
        return "server-config";
    }

    @GetMapping("client/config")
    public String clientConfig() {
        return "client-config";
    }

}

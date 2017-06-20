package com.kongzhong.mrpc.admin.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author biezhi
 *         2017/5/14
 */
@Controller
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @RequestMapping("signin")
    public String signin(Model model, String error, String logout) {
        if (error != null)
            model.addAttribute("message", "用户名或密码错误");
        if (logout != null)
            model.addAttribute("message", "您已退出后台");
        return "signin";
    }

}

package com.kongzhong.mrpc.admin.controller;

import com.kongzhong.mrpc.admin.config.RpcAdminProperties;
import com.kongzhong.mrpc.admin.model.dto.LoginModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static com.kongzhong.mrpc.admin.config.RpcAdminConst.LOGIN_COOKIE_KEY;
import static com.kongzhong.mrpc.admin.config.RpcAdminConst.LOGIN_SESSION_KEY;

/**
 * @author biezhi
 *         2017/5/14
 */
@Slf4j
@Controller
public class AuthController {

    @Autowired
    private RpcAdminProperties rpcAdminProperties;

    @GetMapping("signin")
    public String signin() {
        return "signin";
    }

    @RequestMapping("logout")
    public String logout(HttpSession session) {
        session.removeAttribute(LOGIN_SESSION_KEY);
        return "redirect:/signin";
    }

    @PostMapping("signin")
    public String doSignin(HttpSession session, HttpServletResponse response, Model model, LoginModel loginModel) {

        log.debug("登录参数: {}", loginModel);

        if (null == loginModel) {
            model.addAttribute("error", "请确认用户名和密码");
            return "signin";
        }
        if (!rpcAdminProperties.getUser().equals(loginModel.getUsername()) ||
                !rpcAdminProperties.getPass().equals(loginModel.getPassword())) {
            model.addAttribute("error", "用户名或密码错误");
            return "signin";
        }

        session.setAttribute(LOGIN_SESSION_KEY, loginModel);
        response.addCookie(new Cookie(LOGIN_COOKIE_KEY, loginModel.getUsername()));

        return "redirect:/admin";
    }

}

package com.demo.controller;

import com.kongzhong.mrpc.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author biezhi
 *         2017/4/24
 */
@RestController
public class HomeController {

    @Autowired
    private UserService userService;

    @GetMapping("/home")
    public String home(@RequestParam(defaultValue = "mrpc", name = "name") String name) {
        return userService.hello(name);
    }

}
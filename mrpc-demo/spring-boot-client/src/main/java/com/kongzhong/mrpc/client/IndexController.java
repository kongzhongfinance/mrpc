package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author biezhi
 * 2017/5/15
 */
@RestController
public class IndexController {

    @Autowired
    private UserService userService;

    @GetMapping("/hello")
    public String index(String msg, Integer timeout) {
        if (null != timeout) {
            userService.testTimeout(timeout);
            return "ok";
        }
        return userService.hello(msg);
    }

    @GetMapping("/hystrix")
    public String hystrix(@RequestParam(defaultValue = "0", name = "num") int num) {
        return userService.testHystrix(num);
    }

    @GetMapping("/trace")
    public String trace(){
        return userService.testTrace();
    }
}

package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author biezhi
 * 2017/5/15
 */
@Slf4j
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
    public String trace() {
        String t1 = userService.testTrace();
        log.info("@@ t1={}", t1);
        String t2 = userService.testTrace();
        log.info("@@ t2={}", t2);
        return t1 + t2;
    }
}

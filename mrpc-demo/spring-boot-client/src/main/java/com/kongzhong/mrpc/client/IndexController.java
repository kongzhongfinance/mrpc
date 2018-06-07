package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.demo.model.XXDto;
import com.kongzhong.mrpc.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
            String result = userService.testTimeout(timeout);
            System.out.println("result:" + result);
            return "ok";
        }
        Optional<XXDto> xxDto = userService.testOptional();
        System.out.println("xxDto: " + xxDto);
        return userService.hello(msg);
    }

    @GetMapping("/hystrix")
    public String hystrix(@RequestParam(defaultValue = "0", name = "num") int num) {
        return userService.testHystrix(num);
    }

    @GetMapping("/trace")
    public String trace() {
        String t1 = userService.testTrace();
        return t1;
    }

    @GetMapping("/restart")
    public String restart() {
        String t1 = userService.testRestart();
        System.out.println("t1: " + t1);
        return t1;
    }

}

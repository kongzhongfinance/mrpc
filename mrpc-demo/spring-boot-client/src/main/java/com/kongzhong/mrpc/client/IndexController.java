package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.demo.service.UserService;
import org.apache.zookeeper.Op;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * @author biezhi
 *         2017/5/15
 */
@RestController
public class IndexController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String index(String name, Optional<Integer> timeout) {
        if (timeout.isPresent()) {
            userService.testTimeout(timeout.get());
            return "ok";
        }
        return userService.hello(name);
    }

}

package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.demo.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * @author biezhi
 *         2017/5/15
 */
@RestController
@Lazy
public class IndexController {

    @Resource
    private UserService userService;

    @GetMapping("/hello")
    public String index(String msg, Optional<Integer> timeout) {
        if (timeout.isPresent()) {
            userService.testTimeout(timeout.get());
            return "ok";
        }
        return userService.hello(msg);
    }

}

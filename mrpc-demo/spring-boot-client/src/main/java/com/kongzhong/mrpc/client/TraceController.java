package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.demo.service.PayService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * @author biezhi
 *         2017/5/15
 */
@RestController
public class TraceController {

    @Resource
    private PayService payService;

    @GetMapping("/pay")
    public String index() {
        String msg = payService.pay("hello", new BigDecimal("20"));
        return msg;
    }

}

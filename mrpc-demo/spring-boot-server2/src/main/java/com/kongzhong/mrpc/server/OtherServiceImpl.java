package com.kongzhong.mrpc.server;

import com.kongzhong.mrpc.annotation.RpcService;
import com.kongzhong.mrpc.demo.service.OtherService;
import com.kongzhong.mrpc.demo.service.PayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * @author biezhi
 * @date 2017/11/22
 */
@Slf4j
@RpcService
public class OtherServiceImpl implements OtherService {

    @Autowired
    private PayService payService;

    @Override
    public String waitTime(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
            log.info("休眠了 {} 秒", seconds);
            payService.pay("2", new BigDecimal("22"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.valueOf(seconds);
    }

}

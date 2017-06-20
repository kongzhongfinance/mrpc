package com.kongzhong.mrpc.serverandclient;

import com.kongzhong.mrpc.demo.service.PayService;
import com.kongzhong.mrpc.server.ServerAndClientApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

/**
 * @author biezhi
 *         16/06/2017
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServerAndClientApplication.class, value = "mrpc.test=true")
public class PayServiceTest {

    @Autowired
    private PayService payService;

    @Test
    public void testPay() {
        payService.pay("支付...", new BigDecimal("12.1"));
    }

}

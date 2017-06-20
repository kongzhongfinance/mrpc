package com.kongzhong.mrpc.server;

import com.kongzhong.mrpc.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author biezhi
 *         19/06/2017
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ServerApplication.class, value = {
        "mrpc.test=true"
})
public class ServiceTest {

    @Autowired
    private UserService userService;

    @Test
    public void testHello() {
        String msg = userService.hello("spring-boot");
        Assert.assertEquals("Hello, spring-boot", msg);
    }

}
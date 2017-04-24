package com.kongzhong.mrpc;

import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author biezhi
 *         2017/4/24
 */
@Configuration
public class RpcAutoConfiguration {

    @PostConstruct
    public void init() {
        System.out.println(">>>>>>> init");
    }

}

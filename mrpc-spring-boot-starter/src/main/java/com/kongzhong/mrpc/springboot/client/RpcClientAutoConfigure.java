package com.kongzhong.mrpc.springboot.client;

import com.kongzhong.mrpc.client.BootRpcClient;
import com.kongzhong.mrpc.client.Referers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author biezhi
 *         2017/5/13
 */
@Configuration
@ConditionalOnProperty("mrpc.client.transport")
@Slf4j
public class RpcClientAutoConfigure {

    @Bean
    @ConditionalOnBean(Referers.class)
    public BootRpcClient bootRpcClient() {
        BootRpcClient bootRpcClient = new BootRpcClient();
        return bootRpcClient;
    }

}

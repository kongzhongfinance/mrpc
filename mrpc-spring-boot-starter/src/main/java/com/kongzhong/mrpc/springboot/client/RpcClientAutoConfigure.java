package com.kongzhong.mrpc.springboot.client;

import com.kongzhong.mrpc.client.Referers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * RPC客户端自动配置
 *
 * @author biezhi
 *         2017/5/13
 */
@Slf4j
@ConditionalOnProperty("mrpc.client.transport")
public class RpcClientAutoConfigure {

    @Bean
    @ConditionalOnBean(value = Referers.class)
    public BootRpcClient bootRpcClient() {
        log.debug("Initializing rpc client referers");
        return new BootRpcClient();
    }

}

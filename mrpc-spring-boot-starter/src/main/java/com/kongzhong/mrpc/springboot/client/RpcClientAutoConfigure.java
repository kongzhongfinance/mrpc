package com.kongzhong.mrpc.springboot.client;

import com.kongzhong.mrpc.client.BootRpcClient;
import com.kongzhong.mrpc.client.Referers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * @author biezhi
 *         2017/5/13
 */
@ConditionalOnProperty("mrpc.client.transport")
@Slf4j
public class RpcClientAutoConfigure {

//    @Bean
//    @ConditionalOnBean(Referers.class)
//    public BootRpcClient bootRpcClient() {
//        BootRpcClient bootRpcClient = new BootRpcClient();
//        return bootRpcClient;
//    }

    private BootRpcClient bootRpcClient;

    @Bean
    @ConditionalOnBean(value = BootRpcClient.class)
    public RpcClientInitBean initBean() {
        log.debug("Initializing rpc client bean");
        return new RpcClientInitBean(this.bootRpcClient);
    }

    @Bean
    @ConditionalOnBean(value = Referers.class)
    public BootRpcClient bootRpcClient() {
        log.debug("Initializing rpc client referers");
        this.bootRpcClient = new BootRpcClient();
        return bootRpcClient;
    }

}

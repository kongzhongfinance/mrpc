package com.kongzhong.demo.tcp;

import com.kongzhong.mrpc.client.RpcSpringClient;
import com.kongzhong.mrpc.demo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author biezhi
 *         2017/4/19
 */
public class ExpClientApplication {

    public static final Logger log = LoggerFactory.getLogger(ExpClientApplication.class);

    public static void main(String[] args) throws Exception {
        RpcSpringClient rpcClient = new RpcSpringClient();
        final UserService userService = rpcClient.getProxyReferer(UserService.class);

        try {
            userService.testBizExp(55);
            userService.testNormalExp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        rpcClient.stop();
    }
}

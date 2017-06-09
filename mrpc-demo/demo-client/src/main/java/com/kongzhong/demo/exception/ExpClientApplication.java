package com.kongzhong.demo.exception;

import com.kongzhong.mrpc.client.RpcClient;
import com.kongzhong.mrpc.demo.service.UserService;
import com.kongzhong.mrpc.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author biezhi
 *         2017/4/19
 */
public class ExpClientApplication {

    public static final Logger log = LoggerFactory.getLogger(ExpClientApplication.class);

    public static void main(String[] args) throws Exception {
        RpcClient rpcClient = new RpcClient();
        final UserService userService = rpcClient.getProxyBean(UserService.class);

        try {
            userService.testBizExp(55);
            userService.testNormalExp();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        rpcClient.stop();
    }
}

package com.kongzhong.demo.tcp;

import com.kongzhong.mrpc.client.RpcSpringClient;
import com.kongzhong.mrpc.demo.exception.BizException;
import com.kongzhong.mrpc.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author biezhi
 *         2017/4/19
 */
@Slf4j
public class ExpClientApplication {

    public static void main(String[] args) throws Exception {
        RpcSpringClient rpcClient = new RpcSpringClient();
        rpcClient.setDirectAddress("127.0.0.1:5066");
        final UserService userService = rpcClient.getProxyReferer(UserService.class);

        try {
            userService.testBizExp(55);
            userService.testNormalExp();
        } catch (Exception e) {
            if (e instanceof BizException) {
                BizException bizException = (BizException) e;
                System.out.println(bizException.getCode() + "::" + bizException.getMsg());
            } else {
                e.printStackTrace();
            }
        }
        rpcClient.shutdown();
    }
}

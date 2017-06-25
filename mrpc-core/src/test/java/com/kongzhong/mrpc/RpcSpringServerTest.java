package com.kongzhong.mrpc;

import com.kongzhong.mrpc.server.RpcSpringServer;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Rpc Spring 服务端测试
 *
 * @author biezhi
 *         25/06/2017
 */
public class RpcSpringServerTest extends BaseTestCase {

    private ApplicationContext ctx;

    @Test
    public void testDefaultServerConfig() {
        ctx = new ClassPathXmlApplicationContext("mrpc-server.xml");
        RpcSpringServer rpcSpringServer = ctx.getBean(RpcSpringServer.class);
        Assert.assertEquals(rpcSpringServer.getAppId(), "default");
        Assert.assertEquals(rpcSpringServer.getAddress(), "127.0.0.1:5066");
        Assert.assertEquals(rpcSpringServer.getElasticIp(), "");
        Assert.assertEquals(rpcSpringServer.getInterceptors(), "");
        Assert.assertEquals(rpcSpringServer.getPoolName(), "mrpc-server");
        Assert.assertEquals(rpcSpringServer.getSerialize(), "kyro");
        Assert.assertEquals(rpcSpringServer.getTest(), "true");
        Assert.assertEquals(rpcSpringServer.getTransport(), "tcp");
    }
}

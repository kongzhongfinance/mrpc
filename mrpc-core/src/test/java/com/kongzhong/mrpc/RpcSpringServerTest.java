package com.kongzhong.mrpc;

import com.kongzhong.mrpc.server.RpcSpringServer;
import com.kongzhong.mrpc.service.DemoService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Rpc Spring 服务端测试
 *
 * @author biezhi
 *         25/06/2017
 */
@ContextConfiguration(locations = {"classpath:mrpc-server.xml"})
public class RpcSpringServerTest extends AbstractJUnit4SpringContextTests {

    @Test
    public void testDefaultServerConfig() {
        RpcSpringServer rpcSpringServer = applicationContext.getBean(RpcSpringServer.class);
        assertThat(rpcSpringServer.getAppId(), is("default"));
        assertThat(rpcSpringServer.getAddress(), is("127.0.0.1:5066"));
        assertThat(rpcSpringServer.getElasticIp(), is(""));
        assertThat(rpcSpringServer.getInterceptors(), is(""));
        assertThat(rpcSpringServer.getPoolName(), is("mrpc-server"));
        assertThat(rpcSpringServer.getSerialize(), is("kyro"));
        assertThat(rpcSpringServer.getTest(), is("true"));
        assertThat(rpcSpringServer.getTransport(), is("tcp"));
    }

    @Test
    public void testDemoHello() {
        DemoService demoService = applicationContext.getBean(DemoService.class);
        String result = demoService.hello("jack");
        Assert.assertEquals("mrpc-response-jack", result);
    }

}

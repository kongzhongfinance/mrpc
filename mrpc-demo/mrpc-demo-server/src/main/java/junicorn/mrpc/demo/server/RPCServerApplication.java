package junicorn.mrpc.demo.server;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class RPCServerApplication {

    public static void main(String[] args) {
        System.out.println("RPCServerApplication start...");
        ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext("spring-rpc.xml");
        ctx.registerShutdownHook();
        ctx.start();
    }

}

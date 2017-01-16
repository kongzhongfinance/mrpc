package junicorn.mrpc.demo.server;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class RPCServerApplication2 {

    public static void main(String[] args) {
        System.out.println("RPCServerApplication2 start...");
        ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext("spring-rpc2.xml");
        ctx.registerShutdownHook();
        ctx.start();

    }

}

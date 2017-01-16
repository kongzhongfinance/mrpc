package junicorn.mrpc.demo.client;

import junicorn.mrpc.client.RpcClient;
import junicorn.mrpc.demo.api.AddService;
import junicorn.mrpc.demo.api.UserService;
import junicorn.mrpc.demo.exception.AddServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Client5 {

    private static final Logger logger = LoggerFactory.getLogger(Client5.class);

    public static void main(String[] args) throws Exception {

        final ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext("spring-rpc.xml");
        final RpcClient rpcClient = ctx.getBean(RpcClient.class);

        final AddService addService = rpcClient.create(AddService.class);
        try {
            addService.exception();
        } catch (Exception e){
            System.out.println(e instanceof AddServiceException);
            e.printStackTrace();
        }
        rpcClient.stop();
    }

}

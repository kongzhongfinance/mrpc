package junicorn.mrpc.demo.client;

import junicorn.mrpc.client.RpcClient;
import junicorn.mrpc.demo.api.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Client4 {

    private static final Logger logger = LoggerFactory.getLogger(Client4.class);

    public static void main(String[] args) throws Exception {

        final ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext("spring-rpc.xml");
        final RpcClient rpcClient = ctx.getBean(RpcClient.class);

        final UserService userService = rpcClient.create(UserService.class);

        int threadNum = 8;
        final int requestNum = 10000;

        long startTime = System.currentTimeMillis();

        Thread[] threads = new Thread[threadNum];

        for (int i = 0; i < threadNum; ++i) {
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < requestNum; i++) {
                        try {
                            userService.getUsers(i);
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                    }
                }
            });
            threads[i].start();
        }
        for(int i = 0; i < threads.length; i++) {
            threads[i].join();
        }
        long timeCost = (System.currentTimeMillis() - startTime);
        String msg = String.format("同步调用消耗:%sms, tps=%s/s", timeCost, ((double) (requestNum * threadNum)) / timeCost * 1000);
        System.out.println(msg);
        rpcClient.stop();
    }

}

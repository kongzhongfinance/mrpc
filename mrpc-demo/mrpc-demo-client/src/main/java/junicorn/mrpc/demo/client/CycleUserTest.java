package junicorn.mrpc.demo.client;

import junicorn.mrpc.demo.api.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by biezhi on 2016/11/7.
 */
public class CycleUserTest {

    /**
     * 测试1000个并发生成id会不会重复
     */
    public static void main(String[] args) {

        ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext("spring-rpc.xml");
        UserService userService = (UserService) ctx.getBean("userService");

        int count = 1000;
        CyclicBarrier cyclicBarrier = new CyclicBarrier(count);
        ExecutorService executorService = Executors.newFixedThreadPool(count);
        for (int i = 0; i < count; i++)
            executorService.execute(new CycleUserModel(cyclicBarrier, userService));

        executorService.shutdown();
        while (!executorService.isTerminated()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

//        RpcApplicationContext.getRpcClient().shutdown();
        ctx.close();
    }


}

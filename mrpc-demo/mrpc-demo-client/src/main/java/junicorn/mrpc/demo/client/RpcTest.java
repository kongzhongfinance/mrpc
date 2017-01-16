package junicorn.mrpc.demo.client;

import junicorn.mrpc.demo.api.UserService;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by biezhi on 2016/12/13.
 */
public class RpcTest {

    @Test
    public void test1(){
        ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-rpc.xml");
        UserService userService = (UserService) ctx.getBean("userService");
        try {
            while(true){
                System.out.println(userService.getUsers(1L));
                Thread.sleep(1000 * 3);
            }
        } catch (Exception e){}
    }


}

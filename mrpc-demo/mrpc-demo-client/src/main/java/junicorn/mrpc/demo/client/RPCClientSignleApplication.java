package junicorn.mrpc.demo.client;

import junicorn.mrpc.demo.api.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 测试服务单点故障
 */
public class RPCClientSignleApplication {

    private static final Logger logger = LoggerFactory.getLogger(RPCClientSignleApplication.class);

    public static void main(String[] args) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-rpc.xml");
        UserService userService = (UserService) ctx.getBean("userService");
        long index = 1;
        try {
            while(true){
                System.out.println(userService.getUsers(index++));
                Thread.sleep(1000 * 3);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}

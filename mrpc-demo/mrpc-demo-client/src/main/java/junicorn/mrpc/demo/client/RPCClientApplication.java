package junicorn.mrpc.demo.client;

import junicorn.mrpc.demo.api.UserService;
import junicorn.mrpc.demo.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.math.BigDecimal;
import java.util.Date;

public class RPCClientApplication {

    private static final Logger logger = LoggerFactory.getLogger(RPCClientApplication.class);

    public static void main(String[] args) throws ClassNotFoundException {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-rpc.xml");
        UserService userService = (UserService) ctx.getBean("userService");
        System.out.println(userService.getUsers(1L));
        userService.saveUser(new User(99L, 20, "lilei", new Date(), new BigDecimal(12)));
    }

}

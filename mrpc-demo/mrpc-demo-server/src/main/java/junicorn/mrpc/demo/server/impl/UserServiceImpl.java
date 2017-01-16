package junicorn.mrpc.demo.server.impl;

import junicorn.mrpc.demo.api.DeptService;
import junicorn.mrpc.demo.api.UserService;
import junicorn.mrpc.demo.model.User;
import junicorn.mrpc.spring.annotation.MRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@MRpcService
public class UserServiceImpl implements UserService {

    @Autowired
    private DeptService deptService;

    @Override
    public List<User> getUsers(long uid) {
        System.out.println("接收到uid: " + uid);
        List<User> list = new ArrayList<>();
        list.add(new User(1L, 20, "jack", new Date(), new BigDecimal(12)));
        list.add(new User(2L, 22, "rose", new Date(), new BigDecimal(1.5)));
        list.add(new User(3L, 24, "tom", new Date(), new BigDecimal(99)));
        return list;
    }

    @Override
    public void saveUser(User user) {
        System.out.println("保存用户:" + user);
        deptService.save("组长");
    }
}

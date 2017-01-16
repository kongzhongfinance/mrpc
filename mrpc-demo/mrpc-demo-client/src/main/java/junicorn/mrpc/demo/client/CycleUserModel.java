package junicorn.mrpc.demo.client;

import junicorn.mrpc.demo.api.UserService;

import java.util.concurrent.CyclicBarrier;

/**
 * Created by biezhi on 2016/11/7.
 */
public class CycleUserModel implements Runnable{

    private CyclicBarrier cyclicBarrier;
    private UserService userService;

    public CycleUserModel(CyclicBarrier cyclicBarrier, UserService userService) {
        this.cyclicBarrier = cyclicBarrier;
        this.userService = userService;
    }

    @Override
    public void run() {
        try {
            // 等待所有任务准备就绪
            cyclicBarrier.await();
            // 测试内容
            userService.getUsers(20);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

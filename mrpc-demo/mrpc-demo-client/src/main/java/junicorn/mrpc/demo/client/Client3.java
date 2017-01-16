package junicorn.mrpc.demo.client;

import junicorn.mrpc.client.RpcClient;
import junicorn.mrpc.common.config.Constant;
import junicorn.mrpc.demo.api.AddService;
import junicorn.mrpc.demo.api.UserService;
import junicorn.mrpc.discover.zookeeper.ZookeeperServiceDiscovery;
import junicorn.mrpc.registry.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client3 {

    private static final Logger logger = LoggerFactory.getLogger(Client3.class);

    public static void main(String[] args) throws Exception {
        ServiceDiscovery serviceDiscovery = new ZookeeperServiceDiscovery("127.0.0.1:2181");
        final RpcClient rpcClient = new RpcClient(Constant.PROTOSTUFF, serviceDiscovery);
        final UserService userService = rpcClient.create(UserService.class);

        int threadNum = 16;
        final int requestNum = 100000;

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

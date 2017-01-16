package junicorn.mrpc.demo.client.benchmark;

import junicorn.mrpc.client.RpcClient;
import junicorn.mrpc.common.config.Constant;
import junicorn.mrpc.demo.api.HelloService;
import junicorn.mrpc.discover.zookeeper.ZookeeperServiceDiscovery;
import junicorn.mrpc.registry.ServiceDiscovery;

/**
 * 并发测试
 */
public class Benchmark {

    public static void main(String[] args) throws InterruptedException {

//        ServiceDiscovery serviceDiscovery = new ZookeeperServiceDiscovery("127.0.0.1:2181");
        final RpcClient rpcClient = new RpcClient(Constant.PROTOSTUFF, "127.0.0.1:5066");

        final HelloService syncClient = rpcClient.create(HelloService.class);

        int threadNum = 10;
        final int requestNum = 10000;
        Thread[] threads = new Thread[threadNum];

        long startTime = System.currentTimeMillis();
        //benchmark for sync call
        for(int i = 0; i < threadNum; ++i){
            threads[i] = new Thread(new Runnable(){
                @Override
                public void run() {
                    for (int i = 0; i < requestNum; i++) {
                        String result = syncClient.hello(Integer.toString(i));
//                        if (!result.equals("Hello! " + i))
//                            System.out.print("error = " + result);
                    }
                }
            });
            threads[i].start();
        }
        for(int i=0; i<threads.length;i++){
            threads[i].join();
        }
        long timeCost = (System.currentTimeMillis() - startTime);
        String msg = String.format("Sync call total-time-cost:%sms, req/s=%s",timeCost,((double)(requestNum * threadNum)) / timeCost * 1000);
        System.out.println(msg);

        rpcClient.stop();
    }
}

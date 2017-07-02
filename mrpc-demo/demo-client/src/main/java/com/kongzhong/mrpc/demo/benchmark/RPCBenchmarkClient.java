package com.kongzhong.mrpc.demo.benchmark;

import com.kongzhong.mrpc.client.RpcSpringClient;
import com.kongzhong.mrpc.demo.service.BenchmarkService;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

public class RPCBenchmarkClient extends AbstractBenchmarkClient {

    static Properties properties = new Properties();
    /**
     * 并发的Runable线程，是否使用相同的client进行调用。
     * true：并发线程只使用一个client（bean实例）调用服务。
     * false: 每个并发线程使用不同的Client调用服务
     */
    private static BenchmarkService benchmarkService;
    private static boolean isMultiClient;

    public static void main(String[] args) throws InterruptedException {
        loadProperties();
        int concurrents = Integer.parseInt(properties.getProperty("concurrents"));
        int runtime = Integer.parseInt(properties.getProperty("runtime"));
        String classname = properties.getProperty("classname");
        String params = properties.getProperty("params");
        isMultiClient = Boolean.parseBoolean(properties.getProperty("isMultiClient"));
        if (args.length == 5) {
            concurrents = Integer.parseInt(args[0]);
            runtime = Integer.parseInt(args[1]);
            classname = args[2];
            params = args[3];
            isMultiClient = Boolean.parseBoolean(args[4]);
        }
        ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext("mrpc-client-benchmark.xml");
        benchmarkService = ctx.getBean(BenchmarkService.class);
        TimeUnit.SECONDS.sleep(2);
        new RPCBenchmarkClient().start(concurrents, runtime, classname, params);
    }

    private static void loadProperties() {
        try {
            properties.load(ClassLoader.getSystemResourceAsStream("benchmark.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ClientRunnable getClientRunnable(String classname, String params, CyclicBarrier barrier,
                                            CountDownLatch latch, long startTime, long endTime) {
        BenchmarkService service;
        if (isMultiClient) {
            ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext("mrpc-client-benchmark.xml");

            service = ctx.getBean(BenchmarkService.class);
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            service = benchmarkService;
        }

        Class[] parameterTypes = new Class[]{BenchmarkService.class, String.class, CyclicBarrier.class,
                CountDownLatch.class, long.class, long.class};
        Object[] parameters = new Object[]{service, params, barrier, latch, startTime, endTime};

        ClientRunnable clientRunnable = null;
        try {
            clientRunnable = (ClientRunnable) Class.forName(classname).getConstructor(parameterTypes).newInstance(parameters);
        } catch (InstantiationException | NoSuchMethodException | ClassNotFoundException | IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.getTargetException();
        }

        return clientRunnable;
    }
}
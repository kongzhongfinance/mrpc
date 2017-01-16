package junicorn.mrpc.demo.client;

import junicorn.mrpc.demo.api.AddService;
import junicorn.mrpc.demo.api.MoodService;
import junicorn.mrpc.demo.api.UserService;
import junicorn.mrpc.demo.exception.AddServiceException;
import junicorn.mrpc.demo.model.GENDER;
import junicorn.mrpc.demo.model.Mood;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Client {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private static int requestNum = 100000;
    private static AtomicInteger successUmn = new AtomicInteger(0);
    private static AtomicInteger failedNum = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
    	
        final ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext("spring-rpc.xml");

//		testError((AddService) ctx.getBean("addService"));
//		testError((AddService) ctx.getBean("addService"));
//		testMood((MoodService) ctx.getBean("moodService"));
        testPerf((AddService) ctx.getBean("addService"));
//        testPerf((UserService) ctx.getBean("userService"));

//		final AddService service = (AddService) ctx.getBean("addService");
//		for(int i=0; i<50; i++){
//			new Thread(new Runnable() {
//				@Override
//				public void run() {
//					try {
//						testPerf(service);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			}).start();
//		}
//		Thread.sleep(10000000);

        TimeUnit.SECONDS.sleep(2);
//        RpcApplicationContext.getRpcClient().shutdown();
        ctx.close();
    }

    public static void testPerf(AddService addService) throws Exception {
        long start = System.currentTimeMillis();
        int count = 100000;
        for (int i = 0; i < count; i++) {
            addService.add(i);
        }

        long time = System.currentTimeMillis() - start;
        double qps = (double) 1000 / ( (double)time / (double) count);
        System.out.println("times:" + (System.currentTimeMillis() - start) + ", qps:"+new BigDecimal(qps).setScale(2, BigDecimal.ROUND_HALF_UP)+"/s");
    }

    public static void testPerf(UserService userService) throws Exception {
        long start = System.currentTimeMillis();
        int count = 10000;
        for (int i = 0; i < count; i++) {
            userService.getUsers(i);
        }
        long time = System.currentTimeMillis() - start;
        double qps = (double) 1000 / ( (double)time / (double) count);
        System.out.println("times:" + (System.currentTimeMillis() - start) + ", qps:"+new BigDecimal(qps).setScale(2, BigDecimal.ROUND_HALF_UP)+"/s");
    }

    public static void testError(AddService addService) throws Exception {
        try {
            addService.exception();
            System.err.println("no error.");
        } catch (AddServiceException e) {
            e.printStackTrace();
        }
    }

    public static void testMood(MoodService moodService) throws Exception {
        List<Mood> moodList = new LinkedList<Mood>();
        moodList.add(new Mood());
        moodList.add(new Mood());
        moodList.add(new Mood());
        moodList.add(new Mood());
        moodList.add(new Mood());
        moodService.test();
        System.out.println(moodService.test(1));
        System.out.println(moodService.test("name"));
        System.out.println(moodService.test(1, "name"));
        System.out.println(moodService.test(new Mood()));
        System.out.println(moodService.test(moodList));
        System.out.println(moodService.test(new Mood[] { new Mood() }));
        System.out.println(moodService.test(new int[] { 1, 2, 3 }));
        System.out.println(moodService.test(0, 1));
        System.out.println(moodService.test(GENDER.W));
    }

}

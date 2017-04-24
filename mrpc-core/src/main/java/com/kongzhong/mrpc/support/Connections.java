package com.kongzhong.mrpc.support;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.kongzhong.mrpc.common.thread.RpcThreadPool;
import com.kongzhong.mrpc.config.ClientConfig;
import com.kongzhong.mrpc.transport.SimpleClientHandler;
import com.kongzhong.mrpc.transport.SimpleRequestCallback;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 服务连接管理
 *
 * @author biezhi
 *         2017/4/22
 */
@Slf4j
@Data
@NoArgsConstructor
public class Connections {

    /**
     * 细粒度的可重入锁
     */
    private Lock lock = new ReentrantLock();
    private Condition handlerStatus = lock.newCondition();

    private ClientConfig clientConfig = ClientConfig.me();

    /**
     * 并行处理器个数
     */
    private final static int parallel = Runtime.getRuntime().availableProcessors() * 2;

    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(parallel);

    /**
     * 客户端 消息处理线程池
     */
    private static ListeningExecutorService TPE = MoreExecutors.listeningDecorator((ThreadPoolExecutor) RpcThreadPool.getExecutor(16, -1));

    private List<SimpleClientHandler> simpleClientHandlers = Lists.newCopyOnWriteArrayList();

    private static final class ConnectionsHolder {
        private static final Connections $ = new Connections();
    }

    public static Connections me() {
        return ConnectionsHolder.$;
    }

    public void updateNodes(Set<String> addressList) {
        try {
            lock.lock();
            addressList.forEach(address -> {
                String[] ipAddr = address.split(":");
                //获取IP
                String host = ipAddr[0];
                //获取端口号
                int port = Integer.parseInt(ipAddr[1]);
                this.connect(host, port);
            });
            handlerStatus.signal();
        } finally {
            lock.unlock();
        }
    }

    private void connect(String host, int port) {
        //获取socket的完整地址
        final InetSocketAddress remoteAddr = new InetSocketAddress(host, port);
        while (null == clientConfig.getTransport()) {
            sleep(1);
        }
        TPE.submit(new SimpleRequestCallback(eventLoopGroup, remoteAddr));
    }

    private void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    public SimpleClientHandler getRpcClientHandler() throws Exception {
        lock.lock();
        try {
            if (simpleClientHandlers.size() == 0) {
                handlerStatus.await();// 阻塞
            }
            return simpleClientHandlers.get(0);
        } finally {
            lock.unlock();
        }
    }

    public void addRpcClientHandler(SimpleClientHandler handler) {
        try {
            lock.lock();
            if (simpleClientHandlers.contains(handler)) {
                simpleClientHandlers.set(simpleClientHandlers.indexOf(handler), handler);
            } else {
                simpleClientHandlers.add(handler);
            }
            handlerStatus.signal();
        } finally {
            lock.unlock();
        }
    }

    public List<SimpleClientHandler> getHandlers() throws Exception {
        lock.lock();
        try {
            while (simpleClientHandlers.size() == 0) {
                // 阻塞
                handlerStatus.await();
            }
            return simpleClientHandlers;
        } finally {
            lock.unlock();
        }
    }

    public void remove(SimpleClientHandler handler) {
        simpleClientHandlers.remove(handler);
    }

    public void shutdown() {
        TPE.shutdown();
        eventLoopGroup.shutdownGracefully();
    }

}

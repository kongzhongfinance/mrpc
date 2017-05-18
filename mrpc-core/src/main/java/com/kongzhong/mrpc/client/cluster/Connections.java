package com.kongzhong.mrpc.client.cluster;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 客户端连接管理
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
    private final static int parallel = Runtime.getRuntime().availableProcessors() + 1;

    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(parallel);

    /**
     * 客户端 消息处理线程池
     */
    private static ListeningExecutorService TPE = MoreExecutors.listeningDecorator((ThreadPoolExecutor) RpcThreadPool.getExecutor(16, -1));

    /**
     * 服务和服务提供方客户端映射
     * com.kongzhong.service.UserService -> [127.0.0.1:5066, 127.0.0.1:5067]
     */
    private Multimap<String, SimpleClientHandler> mappings = HashMultimap.create();
    private List<String> aliveServers = Lists.newCopyOnWriteArrayList();

    private static final class ConnectionsHolder {
        private static final Connections $ = new Connections();
    }

    public static Connections me() {
        return ConnectionsHolder.$;
    }

    /**
     * server:port -> serviceNames
     *
     * @param mappings
     */
    public void updateNodes(Map<String, Set<String>> smapping) {
        try {
            lock.lock();
            smapping.forEach((key, serviceNames) -> {
                // 如果不存活则建立连接
                if (!aliveServers.contains(key)) {
                    aliveServers.add(key);
                    String[] ipAddr = key.split(":");
                    //获取IP
                    String host = ipAddr[0];
                    //获取端口号
                    int port = Integer.parseInt(ipAddr[1]);
                    this.connect(Sets.newHashSet(serviceNames), host, port);
                }
            });
            handlerStatus.signal();
        } finally {
            lock.unlock();
        }
    }

    private void connect(Set<String> referNames, String host, int port) {
        //获取socket的完整地址
        final InetSocketAddress remoteAddr = new InetSocketAddress(host, port);
        while (null == clientConfig.getTransport()) {
            sleep(1);
        }
        TPE.submit(new SimpleRequestCallback(referNames, eventLoopGroup, remoteAddr));
    }

    private void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    public void addRpcClientHandler(String serviceName, SimpleClientHandler handler) {
        try {
            lock.lock();
            if (mappings.containsKey(serviceName)) {
                if (!mappings.get(serviceName).contains(handler)) {
                    mappings.put(serviceName, handler);
                }
            } else {
                mappings.put(serviceName, handler);
            }
            handlerStatus.signal();
        } finally {
            lock.unlock();
        }
    }

    public List<SimpleClientHandler> getHandlers(String serviceName) throws Exception {
        lock.lock();
        try {
            while (!mappings.containsKey(serviceName) || mappings.get(serviceName).size() == 0) {
                // 阻塞
                handlerStatus.await();
            }
            return Lists.newArrayList(mappings.get(serviceName));
        } finally {
            lock.unlock();
        }
    }

    /**
     * 客户端移除一个失效的连接
     *
     * @param handler
     */
    public void remove(SimpleClientHandler handler) {
        if (mappings.values().size() > 0 && null != handler && mappings.values().contains(handler)) {
            mappings.values().removeAll(Arrays.asList(handler));
            aliveServers.remove(handler.getServerAddress());
        }
    }

    public void shutdown() {
        TPE.shutdown();
        eventLoopGroup.shutdownGracefully();
    }

}

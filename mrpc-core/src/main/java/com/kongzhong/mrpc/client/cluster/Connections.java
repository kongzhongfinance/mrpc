package com.kongzhong.mrpc.client.cluster;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.kongzhong.mrpc.common.thread.RpcThreadPool;
import com.kongzhong.mrpc.transport.netty.NettyClient;
import com.kongzhong.mrpc.transport.netty.SimpleClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.*;
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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Connections {

    /**
     * 细粒度的可重入锁
     */
    private Lock lock = new ReentrantLock();
    private Condition handlerStatus = lock.newCondition();

    /**
     * 并行处理器个数
     */
    private final static int parallel = Runtime.getRuntime().availableProcessors() + 1;
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(parallel);

    /**
     * 客户端 消息处理线程池
     */
    private static final ListeningExecutorService LISTENING_EXECUTOR_SERVICE = MoreExecutors.listeningDecorator((ThreadPoolExecutor) RpcThreadPool.getExecutor(16, -1));

    /**
     * 服务和服务提供方客户端映射
     * com.kongzhong.service.UserService -> [127.0.0.1:5066, 127.0.0.1:5067]
     */
    private Multimap<String, SimpleClientHandler> mappings = HashMultimap.create();

    /**
     * 当前存货的服务列表
     */
    private List<String> aliveServers = Lists.newCopyOnWriteArrayList();

    private static final class ConnectionsHolder {
        private static final Connections INSTANCE = new Connections();
    }

    public static Connections me() {
        return ConnectionsHolder.INSTANCE;
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
                    this.asyncConnect(Sets.newHashSet(serviceNames), host, port);
                }
            });
            handlerStatus.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * @param serviceName
     * @param address
     */
    public void asyncDirectConnect(String serviceName, String address) {
        try {
            lock.lock();
            aliveServers.add(address);
            String[] ipAddr = address.split(":");

            String host = ipAddr[0];
            int port = Integer.parseInt(ipAddr[1]);

            this.asyncConnect(Sets.newHashSet(serviceName), host, port);
            handlerStatus.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 异步建立连接
     *
     * @param referNames
     * @param host
     * @param port
     * @return
     */
    private ListenableFuture<Bootstrap> syncConnect(Set<String> referNames, String host, int port) {
        //获取socket的完整地址
        final InetSocketAddress remoteAddr = new InetSocketAddress(host, port);
        log.debug("Sync connect {}:{} {}", host, port, referNames);

        return LISTENING_EXECUTOR_SERVICE.submit(() -> new NettyClient(host, port).referers(referNames).createBootstrap(eventLoopGroup));
    }

    /**
     * 同步建立连接
     *
     * @param referNames
     * @param host
     * @param port
     */
    private void asyncConnect(Set<String> referNames, String host, int port) {
        //获取socket的完整地址
        final InetSocketAddress remoteAddr = new InetSocketAddress(host, port);
        log.debug("Async connect {}:{} {}", host, port, referNames);

        new NettyClient(host, port).referers(referNames).createBootstrap(eventLoopGroup);
    }

    private void sleep(int milliscond) {
        try {
            TimeUnit.MILLISECONDS.sleep(milliscond);
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
            while (!mappings.containsKey(serviceName)) {
                return Collections.EMPTY_LIST;
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
            log.info("Remove client {}", handler.getChannel());
            aliveServers.remove(handler.getNettyClient().getServerAddress());
        }
    }

    public void shutdown() {
        LISTENING_EXECUTOR_SERVICE.shutdown();
        eventLoopGroup.shutdownGracefully();
    }

}

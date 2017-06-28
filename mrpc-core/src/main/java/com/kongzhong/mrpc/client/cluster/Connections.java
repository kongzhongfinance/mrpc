package com.kongzhong.mrpc.client.cluster;

import com.google.common.collect.*;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.kongzhong.mrpc.common.thread.RpcThreadPool;
import com.kongzhong.mrpc.config.NettyConfig;
import com.kongzhong.mrpc.transport.netty.NettyClient;
import com.kongzhong.mrpc.transport.netty.SimpleClientHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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
    private volatile Multimap<String, SimpleClientHandler> mappings = HashMultimap.create();

    /**
     * 服务地址和服务名的绑定关系
     * 127.0.0.1:5066 -> [com.kongzhong.service.UserService, com.kongzhong.service.PayService]
     */
    private Multimap<String, String> addressServices = HashMultimap.create();

    /**
     * 当前存活的服务列表
     */
    private List<String> aliveServers = Lists.newCopyOnWriteArrayList();

    /**
     * 挂掉的服务
     */
    private Map<String, Set<String>> dieServices = Maps.newHashMap();

    @Setter
    private NettyConfig nettyConfig = new NettyConfig();

    private static final class ConnectionsHolder {
        private static final Connections INSTANCE = new Connections();
    }

    public static Connections me() {
        return ConnectionsHolder.INSTANCE;
    }

    /**
     * 同步建立连接
     * <p>
     * server:port -> serviceNames
     *
     * @param mappings
     */
    public void asyncConnect(Map<String, Set<String>> smapping) {
        try {
            lock.lock();
            smapping.forEach((address, serviceNames) -> {
                serviceNames.forEach(serviceName -> {
                    addressServices.put(address, serviceName);
                });

                // 如果不存活则建立连接
                if (!aliveServers.contains(address)) {
                    aliveServers.add(address);
                    this.asyncConnect(address);
                }
            });
            handlerStatus.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 同步直连
     *
     * @param serviceName
     * @param addressSet
     */
    public void asyncDirectConnect(String serviceName, Set<String> addressSet) {
        try {
            lock.lock();
            addressSet.forEach(address -> {
                addressServices.put(address, serviceName);
                // 如果不存活则建立连接
                if (!aliveServers.contains(address)) {
                    aliveServers.add(address);
                    this.asyncConnect(address);
                } else {
                    mappings.values().stream()
                            .filter(handler -> handler.getNettyClient().getAddress().equals(address))
                            .findFirst()
                            .ifPresent(handler -> mappings.put(serviceName, handler));
                }
            });
            handlerStatus.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 异步直连
     *
     * @param serviceName
     * @param addressSet
     */
    public void syncDirectConnect(String serviceName, Set<String> addressSet) {
        LISTENING_EXECUTOR_SERVICE.execute(() -> {
            addressSet.forEach(address -> {
                addressServices.put(address, serviceName);
                // 如果不存活则建立连接
                if (!aliveServers.contains(address)) {
                    aliveServers.add(address);
                    this.asyncConnect(address);
                } else {
                    mappings.values().stream()
                            .filter(handler -> handler.getNettyClient().getAddress().equals(address))
                            .findFirst()
                            .ifPresent(handler -> mappings.put(serviceName, handler));
                }
            });
        });
    }

    /**
     * 同步建立连接
     *
     * @param address
     * @return
     */
    private void asyncConnect(String address) {
        log.debug("Async connect {}", address);
        new NettyClient(nettyConfig, address).createBootstrap(eventLoopGroup);
    }

    /**
     * 休眠
     *
     * @param milliscond
     */
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
            log.debug("Add rpc client handler: {}, {}", serviceName, handler);

            if (mappings.containsKey(serviceName)) {
                if (!mappings.get(serviceName).contains(handler)) {
                    dieServices.remove(handler.getNettyClient().getAddress());
                    mappings.put(serviceName, handler);
                }
            } else {
                dieServices.remove(handler.getNettyClient().getAddress());
                mappings.put(serviceName, handler);
            }
            handlerStatus.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 根据服务获取连接
     *
     * @param serviceName
     * @return
     * @throws Exception
     */
    public List<SimpleClientHandler> getHandlers(String serviceName) throws Exception {
        int pos = 0;
        while (!mappings.containsKey(serviceName) && pos < 4) {
            sleep(500);
            pos++;
        }
        return Lists.newArrayList(mappings.get(serviceName));
    }

    /**
     * 客户端移除一个失效的连接
     *
     * @param handler
     */
    public void remove(SimpleClientHandler handler) {
        if (mappings.values().size() > 0 && null != handler && mappings.values().contains(handler)) {

            String address = handler.getNettyClient().getAddress();

            // 添加挂掉的节点
            dieServices.put(address, Sets.newHashSet(addressServices.get(address)));

            mappings.values().removeAll(Arrays.asList(handler));
            log.info("Remove client {}", handler.getChannel());
            aliveServers.remove(handler.getNettyClient().getServerAddress());
            addressServices.removeAll(handler.getNettyClient().getAddress());
        }
    }

    public void shutdown() {
        mappings.values().forEach(simpleClientHandler -> simpleClientHandler.close());
        mappings.clear();
        aliveServers.clear();
        dieServices.clear();
        addressServices.clear();
        eventLoopGroup.shutdownGracefully();
        LISTENING_EXECUTOR_SERVICE.shutdown();
    }

}

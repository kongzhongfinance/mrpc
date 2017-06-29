package com.kongzhong.mrpc.client.cluster;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.kongzhong.mrpc.client.LocalServiceNodeTable;
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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

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
    private volatile Map<String, Set<SimpleClientHandler>> mappings = new ConcurrentHashMap<>();


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
                // 如果尚未加入本地节点表，则添加
                if (!LocalServiceNodeTable.containsNode(address)) {
                    LocalServiceNodeTable.addNewNode(address);
                    this.asyncConnect(address);
                }
                LocalServiceNodeTable.addServices(address, serviceNames);
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
                if (!LocalServiceNodeTable.containsNode(address)) {
                    LocalServiceNodeTable.addNewNode(address);
                    this.asyncConnect(address);
                }

                if (LocalServiceNodeTable.isAlive(address)) {
                    Set<SimpleClientHandler> serviceHandler = mappings.getOrDefault(serviceName, new HashSet<>());
                    mappings.values().stream()
                            .flatMap(handlers -> handlers.stream())
                            .filter(handler -> handler.getNettyClient().getAddress().equals(address))
                            .findFirst()
                            .ifPresent(handler -> serviceHandler.add(handler));
                }
                LocalServiceNodeTable.addService(address, serviceName);
            });
            handlerStatus.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 恢复连接
     *
     * @param smapping
     */
    public void recoverConnect(Set<String> addresses) {
        addresses.forEach(address -> {
            // 设置当前节点状态为连接中
            LocalServiceNodeTable.setNodeConnecting(address);
            this.asyncConnect(address);
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

            LocalServiceNodeTable.setNodeAlive(handler.getNettyClient().getAddress());

            Set<SimpleClientHandler> handlers = mappings.getOrDefault(serviceName, new HashSet<>());

            if (mappings.containsKey(serviceName)) {
                if (!handlers.contains(handler)) {
                    LocalServiceNodeTable.setNodeAlive(handler.getNettyClient().getAddress());
                    handlers.add(handler);
                }
            } else {
                handlers.add(handler);
            }
            mappings.put(serviceName, handlers);
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
        return Lists.newArrayList(mappings.getOrDefault(serviceName, new HashSet<>()));
    }

    /**
     * 客户端移除一个失效的连接
     *
     * @param handler
     */
    public void remove(SimpleClientHandler simpleClientHandler) {

        List<SimpleClientHandler> handlers = mappings.values().stream()
                .flatMap(val -> val.stream())
                .filter(handler -> handler.getNettyClient().getAddress().equals(simpleClientHandler.getNettyClient().getAddress()))
                .collect(Collectors.toList());

        if (null != handlers && handlers.size() > 0) {
            handlers.forEach(handler -> {
                String address = handler.getNettyClient().getAddress();
                // 添加挂掉的节点
                LocalServiceNodeTable.setNodeDead(address);
            });

            handlers.removeAll(Lists.newArrayList(simpleClientHandler));
        }

        log.info("Remove client {}", simpleClientHandler.getChannel());


    }

    public void shutdown() {

        mappings.values().stream()
                .flatMap(val -> val.stream())
                .forEach(handler -> handler.close());

        mappings.clear();
        LocalServiceNodeTable.clear();
        eventLoopGroup.shutdownGracefully();
        LISTENING_EXECUTOR_SERVICE.shutdown();
    }

}

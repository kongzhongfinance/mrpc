package com.kongzhong.mrpc.client;

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
 * 2017/4/22
 */
@Slf4j
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Connections {

    /**
     * 细粒度的可重入锁
     */
    private Lock      lock          = new ReentrantLock();
    private Condition handlerStatus = lock.newCondition();

    /**
     * 并行处理器个数
     */
    private final static int            parallel       = Runtime.getRuntime().availableProcessors() + 1;
    private              EventLoopGroup eventLoopGroup = new NioEventLoopGroup(parallel);

    /**
     * 客户端 消息处理线程池
     */
    private static final ListeningExecutorService LISTENING_EXECUTOR_SERVICE = MoreExecutors.listeningDecorator((ThreadPoolExecutor) RpcThreadPool.getExecutor(16, -1));

    @Setter
    private NettyConfig nettyConfig = new NettyConfig();

    private static final class ConnectionsHolder {
        private static final Connections INSTANCE = new Connections();
    }

    public static Connections me() {
        return ConnectionsHolder.INSTANCE;
    }

    /**
     * 异步建立连接
     * <p>
     * server:port -> serviceNames
     *
     * @param mappings 服务映射关系
     */
    public void asyncConnect(Map<String, Set<String>> mappings) {
        try {
            lock.lock();
            mappings.forEach((address, serviceNames) -> {
                LocalServiceNodeTable.addServices(address, serviceNames);
                serviceNames.forEach(serviceName -> LocalServiceNodeTable.updateServiceNode(serviceName, address));
                if (!LocalServiceNodeTable.isConnected(address)) {
                    this.asyncConnect(address);
                }
            });
            handlerStatus.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 同步建立连接
     * <p>
     * server:port -> serviceNames
     *
     * @param mappings 服务映射关系
     */
    public void syncConnect(Map<String, Set<String>> mappings) {
        try {
            lock.lock();
            mappings.forEach((address, serviceNames) -> {
                LocalServiceNodeTable.addServices(address, serviceNames);
                serviceNames.forEach(serviceName -> LocalServiceNodeTable.updateServiceNode(serviceName, address));
                this.syncConnect(address);
            });
            handlerStatus.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 同步直连
     *
     * @param serviceNames 服务名称列表
     * @param addressSet   服务地址列表
     */
    public void syncDirectConnect(Set<String> serviceNames, Set<String> addressSet) {
        try {
            lock.lock();
            addressSet.forEach(address -> {
                LocalServiceNodeTable.addServices(address, serviceNames);
                serviceNames.forEach(serviceName -> LocalServiceNodeTable.updateServiceNode(serviceName, address));
                this.syncConnect(address);
            });
            handlerStatus.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 同步直连
     *
     * @param serviceName 服务接口全名称
     * @param addressSet  服务地址列表
     */
    public void syncDirectConnect(String serviceName, Set<String> addressSet) {
        try {
            lock.lock();
            addressSet.forEach(address -> {
                LocalServiceNodeTable.addService(address, serviceName);
                LocalServiceNodeTable.updateServiceNode(serviceName, address);
                this.syncConnect(address);
            });
            handlerStatus.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 恢复连接
     *
     * @param serviceMap 服务和节点地址
     */
    public void recoverConnect(Map<String, Set<String>> serviceMap) {

        // 把services绑定的服务修改为addresses
        Set<Map.Entry<String, Set<String>>> entries = serviceMap.entrySet();
        for (Map.Entry<String, Set<String>> entry : entries) {
            String      serviceName = entry.getKey();
            Set<String> addresses   = entry.getValue();
            for (String address : addresses) {
                LocalServiceNodeTable.addIfNotPresent(address);
                LocalServiceNodeTable.reConnecting(address);
                LocalServiceNodeTable.updateServiceNode(serviceName, address);
            }
        }

        // 连接
        serviceMap.values().stream()
                .flatMap(Set::stream)
                .distinct()
                .forEach(this::syncConnect);
    }

    /**
     * 同步建立连接
     *
     * @param address 服务地址
     */
    private void syncConnect(String address) {
        if (LocalServiceNodeTable.isAlive(address)) {
            return;
        }
        log.debug("Sync connect {}", address);
        new NettyClient(nettyConfig, address).syncCreateChannel(eventLoopGroup);
    }

    /**
     * 异步建立连接
     *
     * @param address 服务地址
     */
    private void asyncConnect(String address) {
        log.debug("Async connect {}", address);
        new NettyClient(nettyConfig, address).asyncCreateChannel(eventLoopGroup);
    }

    /**
     * 休眠
     */
    private void sleep() {
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * 根据服务获取连接
     *
     * @param serviceName 服务全名称
     * @return 返回查询到的客户端列表
     */
    public List<SimpleClientHandler> getHandlers(String serviceName) throws Exception {
        return LocalServiceNodeTable.getAliveNodes(serviceName);
    }

    /**
     * 客户端移除一个失效的连接
     *
     * @param address 服务地址
     */
    public void inActive(String address) {
        // 添加挂掉的节点
        LocalServiceNodeTable.setNodeDead(address);
        log.info("Set node [{}] dead", address);
        log.info("Dead services {}", LocalServiceNodeTable.getDeadServices());
    }

    public void shutdown() {
        LocalServiceNodeTable.shutdown();
        eventLoopGroup.shutdownGracefully();
        LISTENING_EXECUTOR_SERVICE.shutdown();
    }

}

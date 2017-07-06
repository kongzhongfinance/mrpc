package com.kongzhong.mrpc.discover;

import com.github.zkclient.IZkChildListener;
import com.github.zkclient.IZkClient;
import com.github.zkclient.IZkStateListener;
import com.github.zkclient.ZkClient;
import com.google.common.collect.Maps;
import com.kongzhong.mrpc.client.Connections;
import com.kongzhong.mrpc.client.LocalServiceNodeTable;
import com.kongzhong.mrpc.config.ClientConfig;
import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.exception.SystemException;
import com.kongzhong.mrpc.model.ClientBean;
import com.kongzhong.mrpc.registry.Constant;
import com.kongzhong.mrpc.registry.ServiceDiscovery;
import com.kongzhong.mrpc.utils.CollectionUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.Watcher;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Zookeeper服务发现
 */
@Slf4j
public class ZookeeperServiceDiscovery implements ServiceDiscovery {

    private IZkClient zkClient;

    @Getter
    @Setter
    private String zkAddr;

    private boolean isInit;

    private IZkChildListener zkChildListener = new ZkChildListener();

    private Map<String, IZkChildListener> subRelate = Maps.newConcurrentMap();

    private Lock lock = new ReentrantLock();

    public ZookeeperServiceDiscovery(String zkAddr) {
        this.zkAddr = zkAddr;
        init();
    }

    private void init() {
        if (isInit) {
            return;
        }
        isInit = true;
        zkClient = new ZkClient(zkAddr);

        log.info("Connect zookeeper server: [{}]", zkAddr);

        zkClient.subscribeStateChanges(new IZkStateListener() {
            @Override
            public void handleStateChanged(Watcher.Event.KeeperState keeperState) throws Exception {
                watchNode(zkClient);
            }

            @Override
            public void handleNewSession() throws Exception {
                watchNode(zkClient);
            }
        });
    }

    @Override
    public void discover(@NonNull ClientBean clientBean) throws Exception {
        log.debug("Discovery {}", clientBean);

        Set<String> addressSet = this.discoveryService(clientBean.getServiceName());
        if (CollectionUtils.isEmpty(addressSet)) {
            String msg = String.format("Can not find any address node on service: [%s]. please check your zookeeper services :)", clientBean.getServiceName());
            throw new SystemException(msg);
        } else {
            // update node list
            Connections.me().asyncDirectConnect(clientBean.getServiceName(), addressSet);
        }
    }

    private Set<String> discoveryService(String serviceName) {
        String appId = ClientConfig.me().getAppId();
        String path = Constant.ZK_ROOT + "/" + appId + "/" + serviceName;
        // 发现地址列表
        Set<String> addressSet = new HashSet<>();
        if (zkClient.exists(path)) {
            List<String> addresses = zkClient.getChildren(path);
            addresses.forEach(addressSet::add);
        }
        if (!subRelate.containsKey(path)) {
            subRelate.put(path, zkChildListener);
            zkClient.subscribeChildChanges(path, zkChildListener);
        }
        return addressSet;
    }

    /**
     * 监听到服务变动
     *
     * @param zkClient
     * @throws RpcException
     */
    private void watchNode(@NonNull final IZkClient zkClient) throws RpcException {
        lock.lock();
        try {
            String appId = ClientConfig.me().getAppId();
            String path = Constant.ZK_ROOT + "/" + appId;

            List<String> serviceList = zkClient.getChildren(path);
            if (CollectionUtils.isEmpty(serviceList)) {
                System.out.println();
                log.warn("Can not find any address node on path: {}. please check your zookeeper services :)\n", path);
            } else {

//                log.debug("Watch node changed: {}", serviceList);

                serviceList.retainAll(LocalServiceNodeTable.getDeadServices());

                log.debug("Dead service changed: {}", serviceList);
                log.debug("Alive services: {}", LocalServiceNodeTable.getAliveServices());

                Set<String> address = serviceList.stream()
                        .map(service -> this.discoveryService(service))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet());

                // update node list
                if (CollectionUtils.isNotEmpty(address)) {
                    log.debug("Update node list: {}", address);
                    Connections.me().recoverConnect(address);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    class ZkChildListener implements IZkChildListener {
        @Override
        public void handleChildChange(String parentPath, List<String> currentChildren) throws Exception {
            if (null != currentChildren && !currentChildren.isEmpty()) {
                watchNode(zkClient);
            }
        }
    }

    public void stop() {
        if (zkClient != null) {
            zkClient.close();
        }
    }

}
package com.kongzhong.mrpc.discover;

import com.github.zkclient.*;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kongzhong.mrpc.client.cluster.Connections;
import com.kongzhong.mrpc.config.ClientConfig;
import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.registry.Constant;
import com.kongzhong.mrpc.registry.ServiceDiscovery;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Zookeeper服务发现
 */
public class ZookeeperServiceDiscovery implements ServiceDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperServiceDiscovery.class);

    private IZkClient zkClient;

    private String zkAddr;

    private boolean isInit;

    private IZkChildListener zkChildListener = new ZkChildListener();

    private Map<String, IZkChildListener> subRelate = Maps.newConcurrentMap();

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

    public void discover() {
        watchNode(zkClient);
    }

    private void watchNode(final IZkClient zkClient) {
        try {
            String appId = ClientConfig.me().getAppId();

            List<String> serviceList = zkClient.getChildren(Constant.ZK_ROOT + "/" + appId);
            if (null == serviceList || serviceList.size() == 0) {
                throw new RpcException(String.format("can not find any address node on path: %s/%s", Constant.ZK_ROOT, appId));
            }

            // { 127.0.0.1:5066 => [UserService, BatService] }
            Map<String, Set<String>> mappings = Maps.newHashMap();
            serviceList.forEach(service -> {
                String servicePath = Constant.ZK_ROOT + "/" + ClientConfig.me().getAppId() + "/" + service;
                if (zkClient.exists(servicePath)) {
                    List<String> addresses = zkClient.getChildren(servicePath);
                    addresses.forEach(address -> {
                        if (!mappings.containsKey(address)) {
                            mappings.put(address, Sets.newHashSet(service));
                        } else {
                            mappings.get(address).add(service);
                        }
                    });
                }

                if (!subRelate.containsKey(servicePath)) {
                    subRelate.put(servicePath, zkChildListener);
                    zkClient.subscribeChildChanges(servicePath, zkChildListener);
                }
                // 重新订阅改变
//                zkClient.unsubscribeChildChanges(servicePath, zkChildListener);
//                zkClient.subscribeChildChanges(servicePath, zkChildListener);
            });

            // update node list
            Connections.me().updateNodes(mappings);
        } catch (Exception e) {
            LOGGER.error("", e);
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

    public String getZkAddr() {
        return zkAddr;
    }

    public void setZkAddr(String zkAddr) {
        this.zkAddr = zkAddr;
    }
}
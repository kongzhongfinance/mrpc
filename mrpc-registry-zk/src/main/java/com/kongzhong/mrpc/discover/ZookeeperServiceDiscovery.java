package com.kongzhong.mrpc.discover;

import com.github.zkclient.IZkChildListener;
import com.github.zkclient.IZkClient;
import com.github.zkclient.IZkStateListener;
import com.github.zkclient.ZkClient;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kongzhong.mrpc.client.cluster.Connections;
import com.kongzhong.mrpc.config.ClientConfig;
import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.registry.Constant;
import com.kongzhong.mrpc.registry.ServiceDiscovery;
import com.kongzhong.mrpc.utils.CollectionUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.Watcher;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public void discover() throws Exception {
        watchNode(zkClient);
    }

    private void watchNode(final IZkClient zkClient) throws RpcException {
        String appId = ClientConfig.me().getAppId();

        List<String> serviceList = zkClient.getChildren(Constant.ZK_ROOT + "/" + appId);
        if (CollectionUtils.isEmpty(serviceList)) {
            throw new RpcException(String.format("Can not find any address node on path: %s/%s. please check your zookeeper services :)", Constant.ZK_ROOT, appId));
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
        });

        // update node list
        Connections.me().asyncConnect(mappings);
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
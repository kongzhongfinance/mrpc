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
import com.kongzhong.mrpc.model.ClientBean;
import com.kongzhong.mrpc.registry.Constant;
import com.kongzhong.mrpc.registry.ServiceDiscovery;
import com.kongzhong.mrpc.utils.CollectionUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.Watcher;

import java.util.HashSet;
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

    @Override
    public void discover(@NonNull ClientBean clientBean) throws Exception {
        log.debug("Discovery {}", clientBean);

        Set<String> addressSet = this.discoveryService(clientBean.getServiceName());
        if (CollectionUtils.isEmpty(addressSet)) {
            log.warn("Can not find any address node on path: {}. please check your zookeeper services :)", clientBean.getServiceName());
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
            addresses.forEach(address -> {
                addressSet.add(address);
            });
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
        String appId = ClientConfig.me().getAppId();
        String path = Constant.ZK_ROOT + "/" + appId;

        List<String> serviceList = zkClient.getChildren(path);
        if (CollectionUtils.isEmpty(serviceList)) {
            log.warn("Can not find any address node on path: {}. please check your zookeeper services :)", path);
        } else {

            log.debug("Watch node changed: {}", serviceList);

            // { 127.0.0.1:5066 => [UserService, BatService] }
            Map<String, Set<String>> mappings = Maps.newHashMap();
            Set<String> dieServices = new HashSet<>();
            Connections.me().getDieServices().values().forEach(value -> {
                value.forEach(val -> dieServices.add(val));
            });

            serviceList.forEach(service -> {
                // 只更新本地缓存的服务列表
                if (dieServices.contains(service)) {
                    Set<String> addressSet = this.discoveryService(service);
                    addressSet.forEach(address -> {
                        if (!mappings.containsKey(address)) {
                            mappings.put(address, Sets.newHashSet(service));
                        } else {
                            mappings.get(address).add(service);
                        }
                    });
                }
            });

            log.debug("Update node list: {}", mappings);

            // update node list
            Connections.me().asyncConnect(mappings);
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
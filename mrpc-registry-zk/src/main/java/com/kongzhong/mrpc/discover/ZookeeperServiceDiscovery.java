package com.kongzhong.mrpc.discover;

import com.github.zkclient.IZkChildListener;
import com.github.zkclient.IZkClient;
import com.github.zkclient.IZkStateListener;
import com.github.zkclient.ZkClient;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kongzhong.mrpc.Const;
import com.kongzhong.mrpc.client.Connections;
import com.kongzhong.mrpc.client.LocalServiceNodeTable;
import com.kongzhong.mrpc.config.ClientConfig;
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
import java.util.stream.Collectors;

/**
 * Zookeeper服务发现
 */
@Slf4j
public class ZookeeperServiceDiscovery implements ServiceDiscovery {

    private IZkClient zkClient;

    @Getter
    @Setter
    private String zkAddress;

    private boolean isInit;

    private IZkChildListener zkChildListener = new ZkChildListener(this);

    private Map<String, IZkChildListener> subRelate = Maps.newConcurrentMap();

    public ZookeeperServiceDiscovery(String zkAddress) {
        this.zkAddress = zkAddress;
        init();
    }

    private void init() {
        if (isInit) {
            return;
        }
        isInit = true;
        zkClient = new ZkClient(zkAddress);

        log.info("Connect zookeeper server: [{}]", zkAddress);

        zkClient.subscribeStateChanges(new IZkStateListener() {
            @Override
            public void handleStateChanged(Watcher.Event.KeeperState keeperState) throws Exception {
                watchNode();
            }

            @Override
            public void handleNewSession() throws Exception {
                watchNode();
            }
        });
    }

    @Override
    public void discover(@NonNull ClientBean clientBean) throws Exception {
        log.debug("Discovery {}", clientBean);

        Set<String> addressSet = this.discoveryService(clientBean.getAppId(), clientBean.getServiceName());
        if (CollectionUtils.isEmpty(addressSet)) {
            System.out.println();

            log.warn("Can not find any address node on service: [{}]. please check your zookeeper services :)", clientBean.getServiceName());

            // 发现不到的服务添加到本地服务缓存表，并设置为挂掉状态
            if (!LocalServiceNodeTable.exists(clientBean.getServiceName())) {
                LocalServiceNodeTable.addService(Const.EMPTY_SERVER, clientBean.getServiceName());
                LocalServiceNodeTable.setNodeDead(Const.EMPTY_SERVER);
                log.warn("Add local dead service [{}]\n", clientBean.getServiceName());
            }

        } else {
            // update node list
            Connections.me().syncDirectConnect(clientBean.getServiceName(), addressSet);
        }
    }

    private Set<String> discoveryService(String appId, String serviceName) {
        String path = Constant.ZK_ROOT + "/" + appId + "/" + serviceName;
        // 发现地址列表
        Set<String> addressSet = Sets.newConcurrentHashSet();
        if (zkClient.exists(path)) {
            addressSet.addAll(zkClient.getChildren(path));
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
     * @param zkClient Zk客户端
     */
    public synchronized void watchNode() {
        String appId = ClientConfig.me().getAppId();
        String path  = Constant.ZK_ROOT + "/" + appId;

        List<String> serviceList = zkClient.getChildren(path);
        if (CollectionUtils.isEmpty(serviceList)) {
            System.out.println();
            log.warn("Can not find any address node on path: {}. please check your zookeeper services :)\n", path);
        } else {

            Set<String>              deadServices = LocalServiceNodeTable.getDeadServices();
            Map<String, Set<String>> serviceMap   = Maps.newConcurrentMap();

            Set<String> updateServices = new HashSet<>();

            if (CollectionUtils.isNotEmpty(deadServices)) {
                serviceList.retainAll(LocalServiceNodeTable.getDeadServices());
                updateServices.addAll(serviceList);

                log.debug("Dead service changed: {}", updateServices);
            } else {
                updateServices.addAll(LocalServiceNodeTable.getAliveServices());
            }

            for (String service : updateServices) {
                Set<String> address = this.discoveryService(appId, service);
                if (null == address || address.isEmpty()) {
                    continue;
                }

                Set<String> newAddresses = filterAddress(address);
                if (!newAddresses.isEmpty()) {
                    serviceMap.put(service, newAddresses);
                }
            }

            // update node list
            long count = serviceMap.values().stream().flatMap(Collection::stream).distinct().count();
            if (count > 0) {
                log.debug("Update node list: {}", serviceMap.values().stream().flatMap(Collection::stream).distinct().collect(Collectors.toList()));
                Connections.me().recoverConnect(serviceMap);
            }
        }
    }

    private Set<String> filterAddress(Set<String> addressSet) {
        return addressSet.stream().filter(address -> {
            return null != address && !address.isEmpty() && !address.endsWith(":0");
        }).collect(Collectors.toSet());
    }


    @Override
    public void stop() {
        if (zkClient != null) {
            zkClient.close();
        }
    }

}
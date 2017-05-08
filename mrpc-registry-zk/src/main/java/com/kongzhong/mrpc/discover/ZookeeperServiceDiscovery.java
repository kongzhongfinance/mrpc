package com.kongzhong.mrpc.discover;

import com.github.zkclient.IZkChildListener;
import com.github.zkclient.IZkClient;
import com.github.zkclient.IZkStateListener;
import com.github.zkclient.ZkClient;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.kongzhong.mrpc.cluster.Connections;
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

    public ZookeeperServiceDiscovery(String zkAddr) {
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

        zkClient.subscribeChildChanges(Constant.ZK_ROOT, new IZkChildListener() {
            @Override
            public void handleChildChange(String s, List<String> list) throws Exception {
                watchNode(zkClient);
            }
        });
    }

    public void discover() {
        watchNode(zkClient);
    }

    private void watchNode(final IZkClient zkClient) {
        try {
            List<String> addressList = zkClient.getChildren(Constant.ZK_ROOT);
            if (null == addressList || addressList.size() == 0) {
                throw new RuntimeException(String.format("can not find any address node on path: %s", Constant.ZK_ROOT));
            }

            // { 127.0.0.1:5066 => [UserService, BatService] }
            Map<String, Set<String>> mappings = Maps.newHashMap();
            for (String node : addressList) {
                String path = Constant.ZK_ROOT + "/" + node;
                byte[] bytes = zkClient.readData(path);
                String address = new String(bytes);
                String[] sp = node.split("_");
                if (!mappings.containsKey(address)) {
                    mappings.put(address, Sets.newHashSet(sp[0]));
                } else {
                    mappings.get(address).add(sp[0]);
                }

            }

            // update node list
            Connections.me().updateNodes(mappings);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    public void stop() {
        if (zkClient != null) {
            zkClient.close();
        }
    }

}
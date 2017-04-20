package com.kongzhong.mrpc.discover;

import com.github.zkclient.IZkChildListener;
import com.github.zkclient.IZkClient;
import com.github.zkclient.IZkStateListener;
import com.github.zkclient.ZkClient;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.kongzhong.mrpc.config.Constant;
import com.kongzhong.mrpc.registry.ServiceDiscovery;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ZookeeperServiceDiscovery implements ServiceDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperServiceDiscovery.class);

    private volatile List<String> dataList = Lists.newArrayList();

    private IZkClient zkClient;

    public ZookeeperServiceDiscovery(String zkAddr) {
        zkClient = new ZkClient(zkAddr);
        watchNode(zkClient);
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

        zkClient.subscribeChildChanges(Constant.ZK_REGISTRY_PATH, new IZkChildListener() {
            @Override
            public void handleChildChange(String s, List<String> list) throws Exception {
                watchNode(zkClient);
            }
        });
    }

    public String discover() {
        String data = null;
        int size = dataList.size();
        if (size > 0) {
            if (size == 1) {
                data = dataList.get(0);
                LOGGER.debug("using only data: {}", data);
            } else {
                data = dataList.get(ThreadLocalRandom.current().nextInt(size));
                LOGGER.debug("using random data: {}", data);
            }
        }
        return data;
    }

    private void watchNode(final IZkClient zkClient) {
        try {
            dataList.clear();
            List<String> addressList = zkClient.getChildren(Constant.ZK_REGISTRY_PATH);
            if (null == addressList || addressList.size() == 0) {
                throw new RuntimeException(String.format("can not find any address node on path: %s", Constant.ZK_REGISTRY_PATH));
            }

            for (String node : addressList) {
                String path = Constant.ZK_REGISTRY_PATH + "/" + node;
                byte[] bytes = zkClient.readData(path);
                String address = new String(bytes);
                if (!Strings.isNullOrEmpty(address)) {
                    dataList.add(address);
                }
            }
            // update node list

//            ConnManager.updateNodes(Sets.newTreeSet(dataList));
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
package com.kongzhong.mrpc.discover;

import com.github.zkclient.IZkChildListener;

import java.util.List;

public class ZkChildListener implements IZkChildListener {

    private ZookeeperServiceDiscovery zookeeperServiceDiscovery;

    public ZkChildListener(ZookeeperServiceDiscovery zookeeperServiceDiscovery) {
        this.zookeeperServiceDiscovery = zookeeperServiceDiscovery;
    }

    @Override
    public void handleChildChange(String parentPath, List<String> currentChildren) throws Exception {
        if (null != currentChildren && !currentChildren.isEmpty()) {
            zookeeperServiceDiscovery.watchNode();
        }
    }
}
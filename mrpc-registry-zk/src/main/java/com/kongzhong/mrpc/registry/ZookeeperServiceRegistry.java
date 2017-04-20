package com.kongzhong.mrpc.registry;

import com.github.zkclient.IZkClient;
import com.github.zkclient.ZkClient;
import com.google.common.base.Strings;
import com.kongzhong.mrpc.config.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZookeeperServiceRegistry implements ServiceRegistry {

    private static final Logger log = LoggerFactory.getLogger(ZookeeperServiceRegistry.class);

    private IZkClient zkClient;

    public ZookeeperServiceRegistry(String zkAddr) {
        zkClient = new ZkClient(zkAddr);
    }

    @Override
    public void register(String data) {
        if (!Strings.isNullOrEmpty(data)) {
            if (!zkClient.exists(Constant.ZK_REGISTRY_PATH)) {
                zkClient.createPersistent(Constant.ZK_REGISTRY_PATH);
            }
            String str = zkClient.createEphemeralSequential(Constant.ZK_MRPC_PATH, data.getBytes());
            log.info("create node [{}]", str);
        }
    }

}
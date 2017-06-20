package com.kongzhong.mrpc.registry;

import com.github.zkclient.IZkClient;
import com.github.zkclient.ZkClient;
import com.kongzhong.mrpc.config.ServerConfig;
import com.kongzhong.mrpc.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务注册
 */
@Slf4j
public class ZookeeperServiceRegistry implements ServiceRegistry {

    private IZkClient zkClient;

    private String serverAddr;
    private String appId;

    public ZookeeperServiceRegistry(String zkAddr) {
        zkClient = new ZkClient(zkAddr);
    }

    @Override
    public void register(String data) {

        if (null == serverAddr || null == appId) {
            this.serverAddr = StringUtils.isNotEmpty(ServerConfig.me().getElasticIp()) ?
                    ServerConfig.me().getElasticIp() : ServerConfig.me().getAddress();
            this.appId = ServerConfig.me().getAppId();
        }

        removeNode(data);
        createNode(data);
    }

    @Override
    public void unregister(String data) {
        removeNode(data);
    }

    private void removeNode(String node) {
        // node path = rootPath + appId + node + address
        String path = Constant.ZK_ROOT + "/" + appId + "/" + node + "/" + serverAddr;
        if (zkClient.exists(path)) {
            if (!zkClient.delete(path)) {
                log.warn("Delete node [{}] fail", path);
            }
        }
    }

    private void createNode(String node) {
        // node path = rootPath + appId + node + address
        String path = Constant.ZK_ROOT + "/" + appId + "/" + node;
        if (!zkClient.exists(path)) {
            zkClient.createPersistent(path, true);
        }

        log.debug("Create node [{}]", path);
        zkClient.createEphemeral(path + "/" + serverAddr, "".getBytes());
    }

}
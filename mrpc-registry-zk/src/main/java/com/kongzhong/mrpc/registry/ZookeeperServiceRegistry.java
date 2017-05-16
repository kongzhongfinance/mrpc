package com.kongzhong.mrpc.registry;

import com.github.zkclient.IZkClient;
import com.github.zkclient.ZkClient;
import com.kongzhong.mrpc.config.ServerConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务注册
 */
@Slf4j
public class ZookeeperServiceRegistry implements ServiceRegistry {

    private IZkClient zkClient;

    public ZookeeperServiceRegistry(String zkAddr) {
        zkClient = new ZkClient(zkAddr);
    }

    @Override
    public void register(String data) {
        removeNode(data);
        createNode(data);
    }

    @Override
    public void unregister(String data) {

    }

    private void removeNode(String node) {
        String host = ServerConfig.me().getHost();
        int port = ServerConfig.me().getPort();
        String appId = ServerConfig.me().getAppId();

        String address = host + ":" + port;

        // node path = rootPath + appId + node + address
        String path = Constant.ZK_ROOT + "/" + appId + "/" + node + "/" + address;
        if (zkClient.exists(path)) {
            if (!zkClient.delete(path)) {
                log.warn("delete node [{}] fail", path);
            }
        }
    }

    private void createNode(String node) {
        String host = ServerConfig.me().getHost();
        int port = ServerConfig.me().getPort();
        String appId = ServerConfig.me().getAppId();
        String address = host + ":" + port;

        // node path = rootPath + appId + node + address
        String path = Constant.ZK_ROOT + "/" + appId + "/" + node;
        if (!zkClient.exists(path)) {
            zkClient.createPersistent(path, true);
        }

        log.debug("create node [{}]", path);
        zkClient.createEphemeral(path + "/" + address, "".getBytes());
    }

}
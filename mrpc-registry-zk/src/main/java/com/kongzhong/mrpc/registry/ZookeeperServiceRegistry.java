package com.kongzhong.mrpc.registry;

import com.github.zkclient.IZkClient;
import com.github.zkclient.ZkClient;
import com.kongzhong.mrpc.config.Constant;
import com.kongzhong.mrpc.model.ClientConfig;
import com.kongzhong.mrpc.model.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务注册
 */
public class ZookeeperServiceRegistry implements ServiceRegistry {

    private static final Logger log = LoggerFactory.getLogger(ZookeeperServiceRegistry.class);

    private IZkClient zkClient;

    private String rootPath;

    public ZookeeperServiceRegistry(String zkAddr) {
        zkClient = new ZkClient(zkAddr);
        rootPath = Constant.ZK_ROOT;
        if (!zkClient.exists(rootPath)) {
            zkClient.createPersistent(rootPath);
        }
    }

    @Override
    public void register(String data) {
        removeNode(data);
        createNode(data);
    }

    private void removeNode(String node) {
        String host = ServerConfig.me().getHost();
        int port = ServerConfig.me().getPort();
        String data = host + ":" + port;
        String path = rootPath + "/" + node + "_" + data;
        if (zkClient.exists(path)) {
            if (!zkClient.delete(path)) {
                log.warn("delete node [{}] fail", path);
            }
        }
    }

    private void createNode(String node) {
        String host = ServerConfig.me().getHost();
        int port = ServerConfig.me().getPort();
        String data = host + ":" + port;
        String path = rootPath + "/" + node + "_" + data;

        log.debug("create node [{}]", path);
        zkClient.createEphemeral(path, data.getBytes());
    }

}
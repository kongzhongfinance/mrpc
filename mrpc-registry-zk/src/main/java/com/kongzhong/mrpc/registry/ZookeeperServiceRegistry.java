package com.kongzhong.mrpc.registry;

import com.github.zkclient.IZkClient;
import com.github.zkclient.ZkClient;
import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.model.ServiceBean;
import com.kongzhong.mrpc.utils.StringUtils;
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
    public boolean register(ServiceBean serviceBean) throws RpcException {
        if (null == serviceBean) {
            throw new RpcException("Service bean not is null");
        }
        removeNode(serviceBean);
        createNode(serviceBean);
        return true;
    }

    @Override
    public void unRegister(ServiceBean serviceBean) throws RpcException {
        if (null == serviceBean) {
            throw new RpcException("Service bean not is null");
        }
        removeNode(serviceBean);
    }

    private void removeNode(ServiceBean serviceBean) {
        String appId = serviceBean.getAppId();
        String node = serviceBean.getServiceName();
        String serverAddr = StringUtils.isNotEmpty(serviceBean.getElasticIp()) ? serviceBean.getElasticIp() : serviceBean.getAddress();

        // node path = rootPath + appId + node + address
        String path = Constant.ZK_ROOT + "/" + appId + "/" + node + "/" + serverAddr;
        if (zkClient.exists(path)) {
            if (!zkClient.delete(path)) {
                log.warn("Delete node [{}] fail", path);
            }
        }
    }

    private void createNode(ServiceBean serviceBean) {
        String appId = serviceBean.getAppId();
        String node = serviceBean.getServiceName();
        String serverAddr = StringUtils.isNotEmpty(serviceBean.getElasticIp()) ? serviceBean.getElasticIp() : serviceBean.getAddress();

        // node path = rootPath + appId + node + address
        String path = Constant.ZK_ROOT + "/" + appId + "/" + node;
        if (!zkClient.exists(path)) {
            zkClient.createPersistent(path, true);
        }

        log.debug("Create node [{}]", path);

        String serviceNode = path + "/" + serverAddr;
        zkClient.createEphemeral(serviceNode, "".getBytes());
    }

}
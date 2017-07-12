package com.kongzhong.mrpc.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kongzhong.mrpc.Const;
import com.kongzhong.mrpc.enums.NodeAliveStateEnum;
import com.kongzhong.mrpc.transport.netty.SimpleClientHandler;
import com.kongzhong.mrpc.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 本地服务节点表
 *
 * @author biezhi
 *         29/06/2017
 */
@Slf4j
public class LocalServiceNodeTable {

    // 本地节点服务表
    private static final Set<ServiceNode> SERVICE_NODES = Sets.newConcurrentHashSet();

    /**
     * 服务和服务提供方客户端映射
     * com.kongzhong.service.UserService -> [127.0.0.1:5066, 127.0.0.1:5067]
     */
    public static final Map<String, Set<String>> SERVICE_MAPPINGS = Maps.newConcurrentMap();

    public static List<SimpleClientHandler> getAliveNodes(String serviceName) {
        Set<String> addresses = LocalServiceNodeTable.SERVICE_MAPPINGS.get(serviceName);
        if (CollectionUtils.isEmpty(addresses)) {
            return new ArrayList<>();
        }

        List<SimpleClientHandler> clientHandlers = new ArrayList<>();

        addresses.forEach(address -> SERVICE_NODES.stream()
                .filter(node -> address.equals(node.getServerAddress()) && node.getAliveState() == NodeAliveStateEnum.ALIVE)
                .findFirst()
                .ifPresent(node -> clientHandlers.add(node.getClientHandler())));

        return clientHandlers;
    }

    /**
     * 获取所有存活的服务列表
     *
     * @return
     */
    public static Set<String> getAliveServices() {
        return SERVICE_NODES.stream()
                .filter(node -> node.getAliveState() == NodeAliveStateEnum.ALIVE)
                .flatMap(node -> node.getServices().stream())
                .collect(Collectors.toSet());
    }

    /**
     * 获取所有挂掉的服务
     *
     * @return
     */
    public static Set<String> getDeadServices() {
        return SERVICE_NODES.stream()
                .filter(node -> node.getAliveState() == NodeAliveStateEnum.DEAD)
                .flatMap(node -> node.getServices().stream())
                .collect(Collectors.toSet());
    }

    /**
     * 添加一个服务节点
     *
     * @param serverAddress
     */
    public static void addNewNode(String serverAddress) {
        ServiceNode serviceNode = new ServiceNode();
        serviceNode.setServerAddress(serverAddress);
        serviceNode.setAliveState(NodeAliveStateEnum.CONNECTING);
        SERVICE_NODES.add(serviceNode);
    }

    /**
     * 像address添加一个服务
     *
     * @param serverAddress
     * @param serviceName
     */
    public static void addService(String serverAddress, String serviceName) {
        if (!LocalServiceNodeTable.containsNode(serverAddress)) {
            LocalServiceNodeTable.addNewNode(serverAddress);
        }
        updateNode(serverAddress, node -> node.getServices().add(serviceName));
    }

    public static void addIfNotPresent(String serverAddress) {
        if (!LocalServiceNodeTable.containsNode(serverAddress)) {
            LocalServiceNodeTable.addNewNode(serverAddress);
        }
    }

    /**
     * 给节点添加服务列表
     *
     * @param serverAddress
     * @param serviceNames
     */
    public static void addServices(String serverAddress, Set<String> serviceNames) {
        if (!LocalServiceNodeTable.containsNode(serverAddress)) {
            LocalServiceNodeTable.addNewNode(serverAddress);
        }
        updateNode(serverAddress, node -> node.getServices().addAll(serviceNames));
    }

    /**
     * 更新节点存活状态为挂掉
     *
     * @param serverAddress
     */
    public static void setNodeDead(String serverAddress) {
        updateNode(serverAddress, (node) -> {
            node.setClientHandler(null);
            node.setAliveState(NodeAliveStateEnum.DEAD);
        });
        SERVICE_MAPPINGS.values().removeAll(Lists.newArrayList(serverAddress));
    }

    /**
     * 更新节点存活状态为存活
     *
     * @param clientHandler
     */
    public static void setNodeAlive(SimpleClientHandler clientHandler) {
        String address = clientHandler.getNettyClient().getAddress();
        updateNode(address, (node) -> {
            node.setClientHandler(clientHandler);
            node.setAliveState(NodeAliveStateEnum.ALIVE);
        });
    }

    /**
     * 更新节点状态为连接中
     *
     * @param serverAddress
     */
    public static void reConnecting(String serverAddress) {
        updateNode(serverAddress, node -> node.setAliveState(NodeAliveStateEnum.CONNECTING));
    }

    /**
     * 更新节点
     *
     * @param serverAddress
     * @param consumer
     */
    public static void updateNode(String serverAddress, Consumer<? super ServiceNode> consumer) {
        findServiceNode(serverAddress).ifPresent(consumer);
    }

    /**
     * 判断节点是否存活
     *
     * @param address
     * @return
     */
    public static boolean isAlive(String address) {
        Optional<ServiceNode> serviceNode = SERVICE_NODES.stream()
                .filter(node -> node.getServerAddress().equals(address))
                .findFirst();
        if (serviceNode.isPresent()) {
            return serviceNode.get().getAliveState() == NodeAliveStateEnum.ALIVE;
        }
        return false;
    }

    /**
     * 判断服务是否已经成功连接
     *
     * @param address
     * @return
     */
    public static boolean isConnected(String address) {
        Optional<ServiceNode> serviceNode = SERVICE_NODES.stream()
                .filter(node -> node.getServerAddress().equals(address))
                .findFirst();
        if (serviceNode.isPresent()) {
            return serviceNode.get().isConnected();
        }
        return false;
    }

    /**
     * 清楚服务节点信息
     */
    public static void shutdown() {
        SERVICE_MAPPINGS.clear();
        SERVICE_NODES.stream()
                .map(node -> node.getClientHandler())
                .forEach(handler -> {
                    handler.getNettyClient().shutdown();
                    handler.close();
                });

        SERVICE_NODES.clear();
    }

    /**
     * 判断是否存在该节点
     *
     * @param serverAddress
     * @return
     */
    public static boolean containsNode(String serverAddress) {
        return findServiceNode(serverAddress).isPresent();
    }

    /**
     * 根据服务地址查找节点详情
     *
     * @param address
     * @return
     */
    private static Optional<ServiceNode> findServiceNode(String address) {
        return SERVICE_NODES.stream()
                .filter(node -> node.getServerAddress().equals(address))
                .findFirst();
    }

    /**
     * 设置服务已连接
     *
     * @param serverAddress
     */
    public static void setConnected(String serverAddress) {
        updateNode(serverAddress, node -> node.setConnected(true));
    }

    /**
     * 更新服务节点
     *
     * @param serviceName
     * @param serverAddress
     */
    public static void updateServiceNode(String serviceName, String serverAddress) {
        Set<String> serviceNodes = SERVICE_MAPPINGS.getOrDefault(serviceName, new HashSet<>());
        serviceNodes.add(serverAddress);
        SERVICE_MAPPINGS.put(serviceName, serviceNodes);

        SERVICE_NODES.stream()
                .filter(serviceNode -> serviceNode.getServerAddress().equals(Const.EMPTY_SERVER))
                .findFirst()
                .ifPresent(serviceNode -> serviceNode.getServices().remove(serviceName));

        SERVICE_NODES.stream()
                .filter(serviceNode -> serviceNode.getServerAddress().equals(serverAddress))
                .findFirst()
                .ifPresent(serviceNode -> {
                    if (!serviceNode.getServices().contains(serviceName)) {
                        serviceNode.getServices().add(serviceName);
                    }
                });
    }

    /**
     * 返回本地注册表是否存在服务
     *
     * @param serviceName
     * @return
     */
    public static boolean exists(String serviceName) {
        return SERVICE_NODES.stream()
                .map(ServiceNode::getServices)
                .flatMap(Collection::stream)
                .filter(localServiceName -> localServiceName.equals(serviceName))
                .count() > 0;
    }

}

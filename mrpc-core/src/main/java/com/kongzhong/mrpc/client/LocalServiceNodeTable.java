package com.kongzhong.mrpc.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kongzhong.mrpc.enums.NodeAliveStateEnum;
import com.kongzhong.mrpc.transport.netty.SimpleClientHandler;
import com.kongzhong.mrpc.utils.CollectionUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 本地服务节点表
 *
 * @author biezhi
 *         29/06/2017
 */
public class LocalServiceNodeTable {

    private static final Set<ServiceNode> SERVICE_NODES = Sets.newConcurrentHashSet();

    /**
     * 服务和服务提供方客户端映射
     * com.kongzhong.service.UserService -> [127.0.0.1:5066, 127.0.0.1:5067]
     */
    static Map<String, Set<String>> SERVICE_MAPPINGS = Maps.newConcurrentMap();

    /**
     * 获取所有服务节点
     *
     * @return
     */
    public static Set<ServiceNode> getServiceNodes() {
        return SERVICE_NODES;
    }

    /**
     * 获取所有存活的节点
     *
     * @return
     */
    public static Set<SimpleClientHandler> getAliveNodes() {
        return SERVICE_NODES.stream()
                .filter(node -> node.getAliveState() == NodeAliveStateEnum.ALIVE)
                .map(node -> node.getClientHandler())
                .collect(Collectors.toSet());
    }

    public static List<SimpleClientHandler> getAliveNodes(String serviceName) {
        Set<String> address = LocalServiceNodeTable.SERVICE_MAPPINGS.get(serviceName);
        if (CollectionUtils.isEmpty(address)) {
            return new ArrayList<>();
        }

        return SERVICE_NODES.stream()
                .filter(node -> node.getAliveState() == NodeAliveStateEnum.ALIVE)
                .filter(node -> address.contains(node.getAddress()))
                .map(node -> node.getClientHandler())
                .collect(Collectors.toList());
    }

    /**
     * 获取所有挂掉的节点
     *
     * @return
     */
    public static Set<SimpleClientHandler> getDeadNodes() {
        return SERVICE_NODES.stream()
                .filter(node -> node.getAliveState() == NodeAliveStateEnum.DEAD)
                .map(node -> node.getClientHandler())
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
     * @param serviceNode
     */
    public static void addNewNode(String address) {
        ServiceNode serviceNode = new ServiceNode();
        serviceNode.setAddress(address);
        serviceNode.setAliveState(NodeAliveStateEnum.CONNECTING);
        SERVICE_NODES.add(serviceNode);
    }

    /**
     * 像address添加一个服务
     *
     * @param address
     * @param serviceName
     */
    public static void addService(String address, String serviceName) {
        if (!LocalServiceNodeTable.containsNode(address)) {
            LocalServiceNodeTable.addNewNode(address);
        }
        updateNode(address, (node) -> node.getServices().add(serviceName));
    }

    public static void addServices(String address, Set<String> serviceNames) {
        if (!LocalServiceNodeTable.containsNode(address)) {
            LocalServiceNodeTable.addNewNode(address);
        }
        updateNode(address, (node) -> node.getServices().addAll(serviceNames));
    }

    /**
     * 更新节点存活状态为挂掉
     *
     * @param address
     */
    public static void setNodeDead(String address) {
        updateNode(address, (node) -> {
            node.setClientHandler(null);
            node.setAliveState(NodeAliveStateEnum.DEAD);
        });
        SERVICE_MAPPINGS.values().removeAll(Lists.newArrayList(address));
    }

    /**
     * 更新节点存活状态为存活
     *
     * @param address
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
     * @param address
     */
    public static void reConnected(String address) {
        updateNode(address, node -> node.setAliveState(NodeAliveStateEnum.CONNECTING));
    }

    /**
     * 更新节点
     *
     * @param address
     * @param consumer
     */
    public static void updateNode(String address, Consumer<? super ServiceNode> consumer) {
        findServiceNode(address).ifPresent(consumer);
    }

    /**
     * 判断节点是否存活
     *
     * @param address
     * @return
     */
    public static boolean isAlive(String address) {
        Optional<ServiceNode> serviceNode = SERVICE_NODES.stream()
                .filter(node -> node.getAddress().equals(address))
                .findFirst();
        if (serviceNode.isPresent()) {
            return serviceNode.get().getAliveState() == NodeAliveStateEnum.ALIVE;
        }
        return false;
    }

    /**
     * 判断节点是否挂掉
     *
     * @param address
     * @return
     */
    public static boolean isDead(String address) {
        Optional<ServiceNode> serviceNode = SERVICE_NODES.stream()
                .filter(node -> node.getAddress().equals(address))
                .findFirst();
        if (serviceNode.isPresent()) {
            return serviceNode.get().getAliveState() == NodeAliveStateEnum.DEAD;
        }
        return true;
    }

    public static boolean isConnected(String address) {
        Optional<ServiceNode> serviceNode = SERVICE_NODES.stream()
                .filter(node -> node.getAddress().equals(address))
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

    public static boolean containsNode(String address) {
        return findServiceNode(address).isPresent();
    }

    private static Optional<ServiceNode> findServiceNode(String address) {
        return SERVICE_NODES.stream()
                .filter(node -> node.getAddress().equals(address))
                .findFirst();
    }

    public static Set<String> getNodeServices(String address) {
        Optional<ServiceNode> serviceNode = findServiceNode(address);
        if (serviceNode.isPresent()) {
            return serviceNode.get().getServices();
        }
        return null;
    }

    public static void setConnected(String address) {
        updateNode(address, (node) -> node.setConnected(true));
    }

    public static void updateServiceNode(String serviceName, String address) {
        Set<String> serviceNodes = SERVICE_MAPPINGS.getOrDefault(serviceName, new HashSet<>());
        serviceNodes.add(address);
        SERVICE_MAPPINGS.put(serviceName, serviceNodes);
    }
}

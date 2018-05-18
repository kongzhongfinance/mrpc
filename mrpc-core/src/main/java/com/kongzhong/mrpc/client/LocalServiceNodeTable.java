package com.kongzhong.mrpc.client;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kongzhong.mrpc.Const;
import com.kongzhong.mrpc.enums.NodeAliveStateEnum;
import com.kongzhong.mrpc.transport.netty.SimpleClientHandler;
import com.kongzhong.mrpc.utils.CollectionUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 本地服务节点表
 *
 * @author biezhi
 * 29/06/2017
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LocalServiceNodeTable {

    // 本地节点服务表
    private static final Set<ServiceNode> SERVICE_NODES = Sets.newConcurrentHashSet();

    /**
     * 服务和服务提供方客户端映射
     * com.kongzhong.service.UserService -> [127.0.0.1:5066, 127.0.0.1:5067]
     */
    public static final Map<String, Set<String>> SERVICE_MAPPINGS = Maps.newConcurrentMap();

    static List<SimpleClientHandler> getAliveNodes(String serviceName) {
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
     * @return 返回所有存活的服务列表
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
     * @return 返回所有挂掉的服务列表
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
     * @param serverAddress 服务地址
     */
    private static void addNewNode(String serverAddress) {
        ServiceNode serviceNode = new ServiceNode();
        serviceNode.setServerAddress(serverAddress);
        serviceNode.setAliveState(NodeAliveStateEnum.CONNECTING);
        SERVICE_NODES.add(serviceNode);
    }

    /**
     * 像address添加一个服务
     *
     * @param serverAddress 服务地址
     * @param serviceName   服务接口全名
     */
    public static void addService(String serverAddress, String serviceName) {
        if (!LocalServiceNodeTable.containsNode(serverAddress)) {
            LocalServiceNodeTable.addNewNode(serverAddress);
        }
        updateNode(serverAddress, node -> node.getServices().add(serviceName));
    }

    static void addIfNotPresent(String serverAddress) {
        if (!LocalServiceNodeTable.containsNode(serverAddress)) {
            LocalServiceNodeTable.addNewNode(serverAddress);
        }
    }

    /**
     * 给节点添加服务列表
     *
     * @param serverAddress 服务地址
     * @param serviceNames  服务接口全名
     */
    static void addServices(String serverAddress, Set<String> serviceNames) {
        if (!LocalServiceNodeTable.containsNode(serverAddress)) {
            LocalServiceNodeTable.addNewNode(serverAddress);
        }
        updateNode(serverAddress, node -> node.getServices().addAll(serviceNames));
    }

    /**
     * 更新节点存活状态为挂掉
     *
     * @param serverAddress 服务地址
     */
    public static void setNodeDead(String serverAddress) {
        updateNode(serverAddress, (node) -> {
            node.setClientHandler(null);
            node.setAliveState(NodeAliveStateEnum.DEAD);
        });
        SERVICE_MAPPINGS.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList())
                .removeAll(Collections.singletonList(serverAddress));
    }

    /**
     * 更新节点存活状态为存活
     *
     * @param clientHandler 客户端连接Handler
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
     * @param serverAddress 服务地址
     */
    static void reConnecting(String serverAddress) {
        if (!isAlive(serverAddress)) {
            updateNode(serverAddress, node -> node.setAliveState(NodeAliveStateEnum.CONNECTING));
        }
    }

    /**
     * 更新节点
     *
     * @param serverAddress 服务地址
     * @param consumer      消费者
     */
    private static void updateNode(String serverAddress, Consumer<? super ServiceNode> consumer) {
        findServiceNode(serverAddress).ifPresent(consumer);
    }

    /**
     * 判断节点是否存活
     *
     * @param address 服务地址
     * @return 返回该服务节点是否是存活状态
     */
    public static boolean isAlive(String address) {
        Optional<ServiceNode> serviceNode = SERVICE_NODES.stream()
                .filter(node -> node.getServerAddress().equals(address))
                .findFirst();
        return serviceNode.isPresent() && serviceNode.get().getAliveState() == NodeAliveStateEnum.ALIVE;
    }

    /**
     * 判断服务是否已经成功连接
     *
     * @param address 服务地址
     * @return 返回该服务节点是否已经开始连接
     */
    static boolean isConnected(String address) {
        Optional<ServiceNode> serviceNode = SERVICE_NODES.stream()
                .filter(node -> node.getServerAddress().equals(address))
                .findFirst();
        return serviceNode.isPresent() && serviceNode.get().isConnected();
    }

    /**
     * 清楚服务节点信息
     */
    public static void shutdown() {
        SERVICE_MAPPINGS.clear();
        SERVICE_NODES.clear();
    }

    /**
     * 判断是否存在该节点
     *
     * @param serverAddress 服务渎职
     * @return 返回是否存在该节点
     */
    private static boolean containsNode(String serverAddress) {
        return findServiceNode(serverAddress).isPresent();
    }

    /**
     * 根据服务地址查找节点详情
     *
     * @param address 服务地址
     * @return 根据服务地址查询服务节点
     */
    private static Optional<ServiceNode> findServiceNode(String address) {
        return SERVICE_NODES.stream()
                .filter(node -> node.getServerAddress().equals(address))
                .findFirst();
    }

    /**
     * 更新服务节点
     *
     * @param serviceName   服务全名称
     * @param serverAddress 服务地址
     */
    static void updateServiceNode(String serviceName, String serverAddress) {
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
     * @param serviceName 服务名称
     * @return 返回本地注册表是否存在该服务
     */
    public static boolean exists(String serviceName) {
        return SERVICE_NODES.stream()
                .map(ServiceNode::getServices)
                .flatMap(Collection::stream)
                .filter(localServiceName -> localServiceName.equals(serviceName))
                .count() > 0;
    }

}

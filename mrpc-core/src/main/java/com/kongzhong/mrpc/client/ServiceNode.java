package com.kongzhong.mrpc.client;

import com.google.common.collect.Sets;
import com.kongzhong.mrpc.enums.NodeAliveStateEnum;
import com.kongzhong.mrpc.transport.netty.SimpleClientHandler;
import lombok.*;

import java.util.Set;

/**
 * 服务节点
 *
 * @author biezhi
 *         29/06/2017
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ServiceNode {

    /**
     * 服务地址，存储在注册中心都地址
     */
    private String              serverAddress;
    /**
     * 客户端连接
     */
    private SimpleClientHandler clientHandler;
    /**
     * 节点存活状态
     */
    private NodeAliveStateEnum  aliveState;
    /**
     * 是否开始尝试连接
     */
    private boolean             connected;
    /**
     * 该节点下都服务列表
     */
    private Set<String> services = Sets.newHashSet();

}

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

    private String address;
    private SimpleClientHandler clientHandler;
    private NodeAliveStateEnum aliveState;
    private boolean connected;
    private Set<String> services = Sets.newHashSet();

}

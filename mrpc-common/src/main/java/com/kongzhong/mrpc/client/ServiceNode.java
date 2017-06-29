package com.kongzhong.mrpc.client;

import com.google.common.collect.Sets;
import com.kongzhong.mrpc.enums.NodeAliveStateEnum;
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
    private NodeAliveStateEnum aliveState;
    private Set<String> services = Sets.newHashSet();

}

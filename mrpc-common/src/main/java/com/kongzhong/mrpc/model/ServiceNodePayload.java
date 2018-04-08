package com.kongzhong.mrpc.model;

import com.kongzhong.mrpc.enums.NodeAliveStateEnum;
import lombok.*;

import java.util.List;

/**
 * 服务节点信息
 * <p>
 * Created by biezhi on 01/07/2017.
 */
@Data
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ServiceNodePayload {

    private String appId;
    private String address;
    private NodeAliveStateEnum aliveState;
    private List<ServiceStatus> services;

}

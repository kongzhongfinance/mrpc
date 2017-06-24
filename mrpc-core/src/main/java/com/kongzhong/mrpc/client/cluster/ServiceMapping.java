package com.kongzhong.mrpc.client.cluster;

import com.kongzhong.mrpc.transport.netty.SimpleClientHandler;
import lombok.Data;
import lombok.ToString;

import java.util.Set;

/**
 * @author biezhi
 *         2017/5/8
 */
@Data
@ToString
public class ServiceMapping {

    private String service;
    private Set<SimpleClientHandler> clientHandlers;

    public ServiceMapping(String service) {
        this.service = service;
    }
}

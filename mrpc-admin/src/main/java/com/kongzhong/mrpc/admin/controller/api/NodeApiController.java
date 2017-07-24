package com.kongzhong.mrpc.admin.controller.api;

import com.kongzhong.mrpc.admin.model.dto.ServerConfig;
import com.kongzhong.mrpc.admin.model.entity.NodeEntity;
import com.kongzhong.mrpc.admin.repository.NodeRepository;
import com.kongzhong.mrpc.client.RpcSpringClient;
import com.kongzhong.mrpc.embedded.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 服务节点API
 * <p>
 * Created by biezhi on 01/07/2017.
 */
@Slf4j
@RestController
@RequestMapping("api/node")
public class NodeApiController {

    @Autowired
    private NodeRepository nodeRepository;

    private RpcSpringClient rpcSpringClient = new RpcSpringClient();

    @GetMapping
    public List<NodeEntity> getNodeServices() {
        return nodeRepository.findAll();
    }

    @PostMapping("get-config")
    public ServerConfig getServerConfig(NodeEntity nodeEntity) {
        ServerConfig serverConfig = new ServerConfig();
        if (null != nodeEntity) {
            rpcSpringClient.setAppId(nodeEntity.getAppId());
            rpcSpringClient.setDirectAddress(nodeEntity.getAddress());
            rpcSpringClient.setTransport(nodeEntity.getTransport());
            ConfigService configService = rpcSpringClient.getProxyReferer(ConfigService.class);
            serverConfig.setBusinessThreadPoolSize(configService.getBusinessThreadPoolSize());
        }
        return serverConfig;
    }

    @PostMapping("set-config")
    public ResponseEntity<String> setServerConfig(String method, String value, NodeEntity nodeEntity) {
        rpcSpringClient.setAppId(nodeEntity.getAppId());
        rpcSpringClient.setDirectAddress(nodeEntity.getAddress());
        rpcSpringClient.setTransport(nodeEntity.getTransport());
        try {
            ConfigService configService = rpcSpringClient.getProxyReferer(ConfigService.class);
            if (method.equalsIgnoreCase("setBusinessThreadPoolSize")) {
                configService.setBusinessThreadPoolSize(Integer.parseInt(value));
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("设置服务端配置异常", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
package com.kongzhong.mrpc.admin.controller.api;

import com.kongzhong.mrpc.admin.model.dto.ClientConfig;
import com.kongzhong.mrpc.admin.model.dto.ServerConfig;
import com.kongzhong.mrpc.admin.model.entity.ClientEntity;
import com.kongzhong.mrpc.admin.model.entity.NodeEntity;
import com.kongzhong.mrpc.admin.repository.ClientRepository;
import com.kongzhong.mrpc.client.RpcSpringClient;
import com.kongzhong.mrpc.embedded.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 服务端、客户端配置API
 * <p>
 * Created by biezhi on 01/07/2017.
 */
@Slf4j
@RestController
@RequestMapping("api/config")
public class ConfigApiController {

    @Autowired
    private ClientRepository clientRepository;

    private RpcSpringClient rpcSpringClient = new RpcSpringClient();

    /**
     * 获取服务端配置
     *
     * @param nodeEntity
     * @return
     */
    @PostMapping("server")
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

    /**
     * 更新服务端配置
     *
     * @param method
     * @param value
     * @param nodeEntity
     * @return
     */
    @PostMapping("server/update")
    public ResponseEntity<String> setServerConfig(String method, String value, NodeEntity nodeEntity) {
        rpcSpringClient.setAppId(nodeEntity.getAppId());
        rpcSpringClient.setDirectAddress(nodeEntity.getAddress());
        rpcSpringClient.setTransport(nodeEntity.getTransport());
        try {
            ConfigService configService = rpcSpringClient.getProxyReferer(ConfigService.class);
            if ("setBusinessThreadPoolSize".equalsIgnoreCase(method)) {
                configService.setBusinessThreadPoolSize(Integer.parseInt(value));
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("设置服务端配置异常", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 获取客户端配置
     *
     * @return
     */
    @PostMapping("client")
    public List<ClientEntity> getClientConfig() {
        List<ClientEntity> clientEntities = clientRepository.findAll();
        return clientEntities;
    }

    /**
     * 更新客户端配置
     *
     * @return
     */
    @PostMapping("client/update")
    public ClientConfig setClientConfig() {
        ClientConfig clientConfig = new ClientConfig();
        return clientConfig;
    }

}
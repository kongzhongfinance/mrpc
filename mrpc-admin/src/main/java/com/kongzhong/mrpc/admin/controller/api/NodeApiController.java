package com.kongzhong.mrpc.admin.controller.api;

import com.kongzhong.mrpc.admin.model.entity.NodeEntity;
import com.kongzhong.mrpc.admin.repository.NodeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping
    public List<NodeEntity> getNodeServices() {
        return nodeRepository.findAll();
    }

}

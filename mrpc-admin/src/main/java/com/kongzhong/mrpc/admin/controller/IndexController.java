package com.kongzhong.mrpc.admin.controller;

import com.kongzhong.mrpc.admin.model.entity.NodeEntity;
import com.kongzhong.mrpc.admin.repository.NodeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 后台页面
 *
 * @author biezhi
 * 2017/5/14
 */
@Slf4j
@Controller
@RequestMapping("admin")
public class IndexController {

    @Autowired
    private NodeRepository nodeRepository;

    @GetMapping
    public String index() {
        return "index";
    }

    @GetMapping("service")
    public String service() {
        return "service";
    }

    @GetMapping("server/config")
    public String serverConfig(Model model) {
        List<NodeEntity> nodeEntities = nodeRepository.findAll();
        model.addAttribute("nodes", nodeEntities);
        return "server-config";
    }

    @GetMapping("client/config")
    public String clientConfig() {
        return "client-config";
    }

}

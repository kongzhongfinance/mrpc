package com.kongzhong.mrpc.admin.controller.api;

import com.kongzhong.mrpc.admin.config.RpcAdminConst;
import com.kongzhong.mrpc.admin.model.entity.NodeEntity;
import com.kongzhong.mrpc.admin.model.entity.ServiceEntity;
import com.kongzhong.mrpc.admin.repository.NodeRepository;
import com.kongzhong.mrpc.admin.repository.ServiceRepository;
import com.kongzhong.mrpc.model.ServiceNodePayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 服务API
 * <p>
 * Created by biezhi on 01/07/2017.
 */
@Slf4j
@RestController
@RequestMapping("api/service")
public class ServiceApiController {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private NodeRepository nodeRepository;

    /**
     * 分页查询服务列表
     *
     * @param pageable 默认15条
     * @return
     */
    @GetMapping
    public Page<ServiceEntity> getServices(@PageableDefault(value = 15, sort = {"updateTime"},
            direction = Sort.Direction.DESC) Pageable pageable) {

        return serviceRepository.findAll(pageable);
    }

    @PostMapping
    public ResponseEntity<Integer> update(@RequestBody ServiceNodePayload serviceNodePayload) {
        log.info("Update: {}", serviceNodePayload);

        long time = Instant.now().getEpochSecond();

        NodeEntity nodeEntity = new NodeEntity();
        nodeEntity.setAddress(serviceNodePayload.getAddress());
        nodeEntity.setStatus(serviceNodePayload.getAvailAble().name());
        nodeEntity.setUpdateTime(time);
        nodeRepository.save(nodeEntity);

        List<ServiceEntity> list = serviceNodePayload.getServices().stream()
                .map(serviceStatus -> {
                    ServiceEntity serviceEntity = new ServiceEntity();
                    serviceEntity.setServiceName(serviceStatus.getServiceName());
                    serviceEntity.setAppId(serviceStatus.getAppId());
                    serviceEntity.setErrorCount(serviceStatus.getErrorCount());
                    serviceEntity.setInvokeCount(serviceStatus.getInvokeCount());
                    serviceEntity.setTimeoutCount(serviceStatus.getTimeoutCount());
                    serviceEntity.setSuccessCount(serviceStatus.getSuccessCount());
                    serviceEntity.setRegistry(serviceStatus.getRegistry());
                    serviceEntity.setVersion(serviceStatus.getVersion());
                    serviceEntity.setUpdateTime(time);
                    return serviceEntity;
                }).collect(Collectors.toList());

        serviceRepository.save(list);

//        RpcAdminConst.serviceNodePayloads.add(serviceNodePayload);
        return ResponseEntity.ok(200);
    }

}

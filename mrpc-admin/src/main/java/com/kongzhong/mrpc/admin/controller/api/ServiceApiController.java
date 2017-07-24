package com.kongzhong.mrpc.admin.controller.api;

import com.kongzhong.mrpc.admin.model.PageList;
import com.kongzhong.mrpc.admin.model.entity.NodeEntity;
import com.kongzhong.mrpc.admin.model.entity.ServiceEntity;
import com.kongzhong.mrpc.admin.repository.NodeRepository;
import com.kongzhong.mrpc.admin.repository.ServiceRepository;
import com.kongzhong.mrpc.model.ServiceNodePayload;
import com.kongzhong.mrpc.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.Predicate;
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
    public PageList<ServiceEntity> getServices(@PageableDefault(value = 15, sort = {"updateTime"},
            direction = Sort.Direction.DESC) Pageable pageable, String search) {

        Specification<ServiceEntity> specification = (root, query, criteriaBuilder) -> {
            if (StringUtils.isNotEmpty(search)) {
                Predicate _name = criteriaBuilder.like(root.get("serviceName"), "%" + search + "%");
                return criteriaBuilder.and(_name);
            }
            return null;
        };
        return PageList.convert(serviceRepository.findAll(specification, pageable));
    }

    @PostMapping
    public ResponseEntity<Integer> update(@RequestBody ServiceNodePayload serviceNodePayload) {
        log.info("Update: {}", serviceNodePayload);

        long time = Instant.now().getEpochSecond();

        NodeEntity nodeEntity = new NodeEntity();
        nodeEntity.setAddress(serviceNodePayload.getAddress());
        nodeEntity.setAppId(serviceNodePayload.getAppId());
        nodeEntity.setStatus(serviceNodePayload.getAliveState().getState());
        nodeEntity.setTransport(serviceNodePayload.getTransport().name());
        nodeEntity.setUpdateTime(time);
        nodeRepository.save(nodeEntity);

        List<ServiceEntity> list = serviceNodePayload.getServices().stream()
                .map(serviceStatus -> {
                    ServiceEntity serviceEntity = new ServiceEntity();
                    serviceEntity.setServiceName(serviceStatus.getServiceName());
                    serviceEntity.setAppId(serviceStatus.getAppId());
                    serviceEntity.setAddress(serviceNodePayload.getAddress());
                    serviceEntity.setAliveState(serviceNodePayload.getAliveState().getState());
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
        return ResponseEntity.ok(200);
    }

}

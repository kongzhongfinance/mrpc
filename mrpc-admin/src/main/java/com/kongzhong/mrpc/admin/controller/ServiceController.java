package com.kongzhong.mrpc.admin.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.kongzhong.mrpc.admin.config.RpcAdminConst;
import com.kongzhong.mrpc.admin.model.TableResponse;
import com.kongzhong.mrpc.model.ServiceStatus;
import com.kongzhong.mrpc.serialize.jackson.JacksonSerialize;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.Map;

import static com.kongzhong.mrpc.admin.config.RpcAdminConst.RPC_SERVICE_STATUS_JMX_KEY;

/**
 * 服务控制器
 *
 * @author biezhi
 *         2017/5/14
 */
@Slf4j
@RestController
@RequestMapping("admin/api")
public class ServiceController {

    @GetMapping("services.json")
    public TableResponse<ServiceStatus> services() {
        TableResponse<ServiceStatus> tableResponse = new TableResponse<>();

        try {
            // now query to get the beans or whatever
            Object result = RpcAdminConst.mBeanServerConnection.invoke(new ObjectName(RPC_SERVICE_STATUS_JMX_KEY),
                    "getServiceStatus", new Object[]{false}, new String[]{boolean.class.getName()});

            log.debug("获取服务状态表: {}", result);

            Map<String, ServiceStatus> serviceStatusMap = JacksonSerialize.parseObject(result.toString(), new TypeReference<Map<String, ServiceStatus>>() {
            });
            tableResponse.setRows(new ArrayList<>(serviceStatusMap.values()));
            tableResponse.setTotal(tableResponse.getRows().size());
        } catch (Exception e) {
            log.error("获取服务状态表失败", e);
        }
        return tableResponse;
    }

}

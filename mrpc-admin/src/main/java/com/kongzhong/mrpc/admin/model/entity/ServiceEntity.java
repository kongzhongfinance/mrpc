package com.kongzhong.mrpc.admin.model.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 服务实体
 * <p>
 * Created by biezhi on 11/07/2017.
 */
@Data
@Entity
@Table(name = "mrpc_service")
public class ServiceEntity implements Serializable {

    @Id
    private String serviceName;
    private String appId;
    private String version;
    private String registry;
    private Long   invokeCount;
    private Long   successCount;
    private Long   timeoutCount;
    private Long   errorCount;
    private Long   updateTime;

}

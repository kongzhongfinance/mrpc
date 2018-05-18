package com.kongzhong.mrpc.admin.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 服务节点关联实体
 * <p>
 * Created by biezhi on 11/07/2017.
 */
@Data
@Entity
@Table(name = "mrpc_service_node")
public class ServiceNodeEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long   id;
    private String serviceName;
    private String address;

}

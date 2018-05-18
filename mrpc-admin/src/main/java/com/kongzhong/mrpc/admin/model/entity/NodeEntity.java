package com.kongzhong.mrpc.admin.model.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 服务节点实体
 * <p>
 * Created by biezhi on 11/07/2017.
 */
@Data
@Entity
@Table(name = "mrpc_node")
public class NodeEntity implements Serializable {

    @Id
    private String address;
    private String appId;
    private String status;
    private String transport;
    private Long   updateTime;

}

package com.kongzhong.mrpc.admin.vo;

import com.kongzhong.mrpc.admin.model.RpcServer;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @author biezhi
 * @date 2018/6/7
 */
@Data
public class ServerDetailVO {

    private Long   id;
    private String name;
    private String owner;

    private Set<String>     services;
    private List<RpcServer> nodes;

}

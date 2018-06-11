package com.kongzhong.mrpc.admin.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * @author biezhi
 * @date 2018/6/7
 */
@Data
public class ServerMap {

    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ServerMap> children;

    public ServerMap() {

    }

    public ServerMap(String name) {
        this.name = name;
    }
}

package com.kongzhong.mrpc.admin.vo;

import lombok.Data;

import java.util.List;

/**
 * @author biezhi
 * @date 2018/6/7
 */
@Data
public class ServerMap {

    private String name;
    private List<ServerMap> children;

}

package com.kongzhong.mrpc.admin.model;

import io.github.biezhi.anima.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author biezhi
 * @date 2018/6/7
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RpcService extends Model {

    private Long   id;
    private String appId;
    private String serviceId;
    private String serviceAlias;

}

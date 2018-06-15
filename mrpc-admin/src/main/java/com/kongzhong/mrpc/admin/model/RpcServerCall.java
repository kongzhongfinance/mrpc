package com.kongzhong.mrpc.admin.model;

import io.github.biezhi.anima.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author biezhi
 * @date 2018/6/11
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RpcServerCall extends Model {

    private Long id;
    private String producerAppId;
    private String consumerAppId;

}

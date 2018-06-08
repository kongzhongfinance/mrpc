package com.kongzhong.mrpc.admin.model;

import io.github.biezhi.anima.Model;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author biezhi
 * @date 2018/6/7
 */
@Data
public class RpcNotice extends Model {

    private Long          id;
    private String        address;
    private String        apiType;
    private String        content;
    private LocalDateTime createdTime;

}

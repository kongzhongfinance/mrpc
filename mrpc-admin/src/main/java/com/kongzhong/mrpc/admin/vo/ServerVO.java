package com.kongzhong.mrpc.admin.vo;

import com.blade.kit.json.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author biezhi
 * @date 2018/6/7
 */
@Data
public class ServerVO {

    private Long    id;
    private String  name;
    private String  address;
    private Integer pid;
    private String  status;
    private String  owner;

    @JsonFormat("yyyy/MM/dd HH:mm:ss")
    private LocalDateTime onlineTime;

}

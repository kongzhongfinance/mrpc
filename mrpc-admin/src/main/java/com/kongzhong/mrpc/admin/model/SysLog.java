package com.kongzhong.mrpc.admin.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.biezhi.anima.Model;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author biezhi
 * @date 2018/6/11
 */
@Data
public class SysLog extends Model {

    private Long          id;
    private String        action;
    private String        username;
    private String        content;
    private String        ip;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createdTime;

}

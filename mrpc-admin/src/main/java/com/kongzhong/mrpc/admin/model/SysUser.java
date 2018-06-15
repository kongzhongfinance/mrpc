package com.kongzhong.mrpc.admin.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.biezhi.anima.Model;
import io.github.biezhi.anima.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@Table(pk = "user_id")
@EqualsAndHashCode(callSuper = true)
public class SysUser extends Model {

    private Long    userId;
    private String  username;
    private String  password;
    private String  email;
    private String  mobile;
    private Integer status;
    private String  remark;
    private Long    createdId;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createdTime;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime modifiedTime;

}

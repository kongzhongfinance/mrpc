package com.kongzhong.mrpc.admin.model;

import lombok.Data;
import lombok.ToString;

/**
 * Created by biezhi on 01/07/2017.
 */
@Data
@ToString
public class LoginModel {

    private String username;
    private String password;
    private String jmxUrl;

}

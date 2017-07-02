package com.kongzhong.mrpc.config;

import lombok.Data;

/**
 * Created by biezhi on 01/07/2017.
 */
@Data
public class AdminConfig {

    private boolean enabled;
    private String url;
    private String username;
    private String password;
    private int period;

}

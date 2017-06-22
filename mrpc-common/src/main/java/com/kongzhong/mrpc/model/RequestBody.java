package com.kongzhong.mrpc.model;

import lombok.*;

import java.util.List;

/**
 * Http请求Body对象
 *
 * @author biezhi
 *         2017/4/22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RequestBody {

    private String requestId;
    private String service;
    private String method;
    private String version;
    private List<Object> parameters;
    private List<String> parameterTypes;

}
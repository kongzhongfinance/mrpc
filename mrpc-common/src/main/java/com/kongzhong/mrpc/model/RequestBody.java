package com.kongzhong.mrpc.model;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * Http请求Body对象
 *
 * @author biezhi
 *         2017/4/22
 */
@Data
@ToString
public class RequestBody {

    private String requestId;
    private String service;
    private String method;
    private String version;
    private JSONObject parameters;
    private JSONArray parameterTypes;
    private JSONArray parameterArray;

}

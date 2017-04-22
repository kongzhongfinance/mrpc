package com.kongzhong.mrpc.model;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * @author biezhi
 *         2017/4/22
 */
@Data
@ToString
public class RequestBody {

    private String service;
    private String method;
    private String requestId;
    private String version;
    private JSONObject parameters;
    private List<String> parameterTypes;
    private JSONArray parameterArray;

}

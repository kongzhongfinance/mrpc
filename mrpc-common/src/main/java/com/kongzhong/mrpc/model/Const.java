package com.kongzhong.mrpc.model;

/**
 * mrpc常量
 *
 * @author biezhi
 *         2017/6/9
 */
public interface Const {

    // http头信息请求id key
    String HEADER_REQUEST_ID = "requestId";

    // 拦截器名称前缀
    String INTERCEPTOR_NAME_PREFIX = "mrpc:server:interceptor:";

    // 测试环境
    String TEST_ENV = "mrpc.test=true";

}

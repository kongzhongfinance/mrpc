package com.kongzhong.mrpc.trace.interceptor;

import com.kongzhong.basic.zipkin.agent.AbstractAgent;
import com.kongzhong.basic.zipkin.agent.KafkaAgent;
import lombok.extern.slf4j.Slf4j;

/**
 * @author biezhi
 * @date 2017/12/1
 */
@Slf4j
public class BaseFilter {

    protected AbstractAgent agent;

    protected void init(String url, String topic){
        try {
            this.agent = new KafkaAgent(url, topic);
        } catch (Exception e) {
            log.error("初始化Trace服务端失败", e);
        }
    }

}

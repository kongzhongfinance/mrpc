package com.kongzhong.mrpc.ha.loadblance;

import com.kongzhong.mrpc.transport.SimpleClientHandler;

/**
 * Created by biezhi on 2016/12/30.
 */
public interface LoadBalance {

    SimpleClientHandler getClientHandler();

}
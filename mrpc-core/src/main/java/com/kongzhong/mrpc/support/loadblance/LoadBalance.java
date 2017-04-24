package com.kongzhong.mrpc.support.loadblance;

import com.kongzhong.mrpc.client.RpcInvoker;

import java.util.List;

/**
 * Created by biezhi on 2016/12/30.
 */
public interface LoadBalance {

    RpcInvoker getInvoker();

    List<RpcInvoker> getInvokers();

}
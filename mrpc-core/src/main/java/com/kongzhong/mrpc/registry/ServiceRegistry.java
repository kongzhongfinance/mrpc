package com.kongzhong.mrpc.registry;

import com.kongzhong.mrpc.exception.InitializeException;
import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.model.ServiceBean;

/**
 * 服务注册接口
 *
 * @author biezhi
 */
public interface ServiceRegistry {

    /**
     * 注册一个服务
     *
     * @param serviceBean
     * @throws RpcException
     */
    void register(ServiceBean serviceBean) throws RpcException;

    /**
     * 卸载一个服务
     *
     * @param serviceBean
     * @throws RpcException
     */
    void unregister(ServiceBean serviceBean) throws RpcException;
}
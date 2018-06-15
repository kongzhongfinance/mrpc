package com.kongzhong.mrpc.registry;

import com.kongzhong.mrpc.exception.RpcException;
import com.kongzhong.mrpc.model.ServiceBean;

import java.util.List;

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
    boolean register(ServiceBean serviceBean) throws RpcException;

    /**
     * 注册一组服务
     *
     * @param serviceBeans
     */
    default void registerList(List<ServiceBean> serviceBeans) {
        serviceBeans.stream().forEach(this::register);
    }

    /**
     * 卸载一个服务
     *
     * @param serviceBean
     * @throws RpcException
     */
    void unRegister(ServiceBean serviceBean) throws RpcException;

    /**
     * 卸载一组服务
     *
     * @param serviceBeans
     */
    default void unRegisterList(List<ServiceBean> serviceBeans) {
        serviceBeans.stream().forEach(this::unRegister);
    }

}
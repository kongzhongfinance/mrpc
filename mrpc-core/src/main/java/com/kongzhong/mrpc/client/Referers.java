package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.annotation.Command;
import com.kongzhong.mrpc.config.ClientConfig;
import com.kongzhong.mrpc.model.ClientBean;
import com.kongzhong.mrpc.utils.StringUtils;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 客户端引用
 *
 * @author biezhi
 * 2017/5/13
 */
@ToString
public class Referers {

    /**
     * 客户端引用列表
     */
    private List<ClientBean> referers = new ArrayList<>();

    /**
     * 添加客户端引用列表
     *
     * @param types 客户端引用类型
     * @return 返回Referers
     */
    public Referers add(Class<?>... types) {
        Stream.of(types).forEach(type -> referers.add(this.getClientBean(type)));
        return this;
    }

    private ClientBean getClientBean(Class<?> type) {
        ClientBean clientBean = new ClientBean(type);
        Command    command    = type.getAnnotation(Command.class);
        if (null != command && StringUtils.isNotEmpty(command.appId())) {
            clientBean.setAppId(command.appId());
        }
        return clientBean;
    }

    public List<ClientBean> getReferers() {
        return referers;
    }

}

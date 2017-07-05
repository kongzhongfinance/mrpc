package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.model.ClientBean;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 客户端引用
 *
 * @author biezhi
 *         2017/5/13
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
     * @param types
     * @return
     */
    public Referers add(Class<?>... types) {
        Stream.of(types).forEach(type -> referers.add(new ClientBean(type)));
        return this;
    }

    public List<ClientBean> getReferers() {
        return referers;
    }

}

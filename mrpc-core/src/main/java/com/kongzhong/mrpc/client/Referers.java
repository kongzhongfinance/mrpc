package com.kongzhong.mrpc.client;

import com.kongzhong.mrpc.model.ClientBean;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author biezhi
 *         2017/5/13
 */
@ToString
public class Referers {

    private List<ClientBean> referers = new ArrayList<>();

    public Referers add(Class<?>... types) {
        Stream.of(types).forEach(type -> referers.add(new ClientBean(type)));
        return this;
    }

    public Referers add(Class<?> type, String registry) {
        return null;
    }

    public List<ClientBean> getReferers() {
        return referers;
    }

}

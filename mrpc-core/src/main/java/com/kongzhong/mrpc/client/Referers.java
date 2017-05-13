package com.kongzhong.mrpc.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author biezhi
 *         2017/5/13
 */
public class Referers {

    private List<Class<?>> referers = new ArrayList<>();

    public Referers add(Class<?>... types) {
        referers.addAll(Arrays.asList(types));
        return this;
    }

    public List<Class<?>> getReferers() {
        return this.referers;
    }
}

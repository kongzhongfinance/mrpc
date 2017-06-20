package com.kongzhong.mrpc.demo.model;

import lombok.NoArgsConstructor;

/**
 * @author biezhi
 *         20/06/2017
 */
public class NoConstructor {

    private String name;

    public NoConstructor(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "NoConstructor{" +
                "name='" + name + '\'' +
                '}';
    }
}

package com.kongzhong.mrpc.trace.util;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

/**
 * Yamls
 */
public final class Yamls {

    private static final Yaml yaml = new Yaml();

    private Yamls(){}

    public static <T> T load(InputStream in, Class<T> clazz){
        return yaml.loadAs(in, clazz);
    }
}

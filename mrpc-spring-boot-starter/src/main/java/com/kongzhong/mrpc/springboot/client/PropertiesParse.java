package com.kongzhong.mrpc.springboot.client;

import com.google.common.collect.Maps;
import com.kongzhong.mrpc.enums.LBStrategy;
import com.kongzhong.mrpc.springboot.config.CommonProperties;
import com.kongzhong.mrpc.springboot.config.RpcClientProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author biezhi
 *         21/06/2017
 */
@Slf4j
public class PropertiesParse {

    public static RpcClientProperties getRpcClientProperties(ConfigurableEnvironment configurableEnvironment) {
        RpcClientProperties rpcClientProperties = new RpcClientProperties();
        if (null == configurableEnvironment) {
            return rpcClientProperties;
        }
        rpcClientProperties.setAppId(configurableEnvironment.getProperty("mrpc.client.appId", "default"));
        rpcClientProperties.setTransport(configurableEnvironment.getProperty("mrpc.client.transport", "tcp"));
        rpcClientProperties.setSerialize(configurableEnvironment.getProperty("mrpc.client.serialize", "kyro"));
        rpcClientProperties.setLbStrategy(configurableEnvironment.getProperty("mrpc.client.lb-strategy", LBStrategy.ROUND.name()));
        rpcClientProperties.setHaStrategy(configurableEnvironment.getProperty("mrpc.client.ha-strategy"));
        rpcClientProperties.setDirectAddress(configurableEnvironment.getProperty("mrpc.client.direct-address"));
        return rpcClientProperties;
    }

    public static CommonProperties getCommonProperties(ConfigurableEnvironment configurableEnvironment) {
        CommonProperties commonProperties = new CommonProperties();
        if (null == configurableEnvironment) {
            return commonProperties;
        }
        commonProperties.setTest(configurableEnvironment.getProperty("mrpc.test", "false"));

        Map<String, Map<String, String>> registry = Maps.newHashMap();
        Map<String, String> registries = getPropertiesStartingWith(configurableEnvironment, "mrpc.registry");
        registries.forEach((key, value) -> {
            // mrpc.registry[default].type
            String key_ = key.substring(key.indexOf('[') + 1, key.indexOf(']'));
            String field = key.substring(key.indexOf(']') + 2);
            Map<String, String> v = Maps.newHashMap();
            v.put(field, value);
            registry.put(key_, v);
        });

        commonProperties.setRegistry(registry);

        Map<String, Map<String, String>> custom = Maps.newHashMap();

        Map<String, String> customs = getPropertiesStartingWith(configurableEnvironment, "mrpc.custom");
        customs.forEach((key, value) -> {
            // mrpc.registry[default].type
            String key_ = key.substring(key.indexOf('[') + 1, key.indexOf(']'));
            String field = key.substring(key.indexOf(']') + 2);
            Map<String, String> v = Maps.newHashMap();
            v.put(field, value);
            custom.put(key_, v);
        });
        commonProperties.setCustom(custom);
        return commonProperties;
    }

    public static <V> Map<String, V> getPropertiesStartingWith(ConfigurableEnvironment aEnv, String aKeyPrefix) {
        Map<String, V> result = new HashMap<>();
        Map<String, V> map = getAllProperties(aEnv);
        for (Map.Entry<String, V> entry : map.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(aKeyPrefix)) {
                result.put(key, entry.getValue());
            }
        }
        return result;
    }

    public static <V> Map<String, V> getAllProperties(ConfigurableEnvironment aEnv) {
        Map<String, V> result = new HashMap<>();
        aEnv.getPropertySources().forEach(ps -> addAll(result, getAllProperties(ps)));
        return result;
    }

    public static <V> Map<String, V> getAllProperties(PropertySource<?> aPropSource) {
        Map<String, V> result = new HashMap<>();

        if (aPropSource instanceof CompositePropertySource) {
            CompositePropertySource cps = (CompositePropertySource) aPropSource;
            cps.getPropertySources().forEach(ps -> addAll(result, getAllProperties(ps)));
            return result;
        }

        if (aPropSource instanceof EnumerablePropertySource<?>) {
            EnumerablePropertySource<?> ps = (EnumerablePropertySource<?>) aPropSource;
            Arrays.asList(ps.getPropertyNames()).forEach(key -> result.put(key, (V) ps.getProperty(key)));
            return result;
        }
        // note: Most descendants of PropertySource are EnumerablePropertySource. There are some
        // few others like JndiPropertySource or StubPropertySource
        log.debug("Given PropertySource is instanceof " + aPropSource.getClass().getName() + " and cannot be iterated");
        return result;
    }

    private static <V> void addAll(Map<String, V> aBase, Map<String, V> aToBeAdded) {
        for (Map.Entry<String, V> entry : aToBeAdded.entrySet()) {
            if (aBase.containsKey(entry.getKey())) {
                continue;
            }
            aBase.put(entry.getKey(), entry.getValue());
        }
    }

}

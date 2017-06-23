package com.kongzhong.mrpc.springboot.client;

import com.google.common.collect.Maps;
import com.kongzhong.mrpc.Const;
import com.kongzhong.mrpc.config.NettyConfig;
import com.kongzhong.mrpc.enums.LbStrategyEnum;
import com.kongzhong.mrpc.exception.SystemException;
import com.kongzhong.mrpc.springboot.config.CommonProperties;
import com.kongzhong.mrpc.springboot.config.RpcClientProperties;
import com.kongzhong.mrpc.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.kongzhong.mrpc.Const.*;

/**
 * RPC配置文件解析
 *
 * @author biezhi
 *         21/06/2017
 */
@Slf4j
public class PropertiesParse {

    /**
     * 解析RpcClient配置
     *
     * @param env
     * @return
     */
    public static RpcClientProperties getRpcClientProperties(ConfigurableEnvironment env) {
        if (null == env) {
            throw new SystemException("ConfigurableEnvironment not is null");
        }

        RpcClientProperties clientProperties = new RpcClientProperties();
        clientProperties.setAppId(env.getProperty(APP_ID_CLIENT, "default"));
        clientProperties.setTransport(env.getProperty(TRANSPORT_CLIENT, "tcp"));
        clientProperties.setSerialize(env.getProperty(SERIALIZE_CLIENT, "kyro"));
        clientProperties.setLbStrategy(env.getProperty(LB_STRATEGY_S1_CLIENT, env.getProperty(LB_STRATEGY_S2_CLIENT, LbStrategyEnum.ROUND.name())));
        clientProperties.setHaStrategy(env.getProperty(LB_STRATEGY_S1_CLIENT, env.getProperty(LB_STRATEGY_S2_CLIENT)));
        clientProperties.setDirectAddress(env.getProperty(DIRECT_ADDRESS_S1_CLIENT, env.getProperty(DIRECT_ADDRESS_S2_CLIENT)));
        clientProperties.setWaitTimeout(Integer.valueOf(env.getProperty(WAIT_TIMEOUT_S1_CLIENT, env.getProperty(WAIT_TIMEOUT_S2_CLIENT, "10"))));
        clientProperties.setFailOverRetry(Integer.valueOf(env.getProperty(FAILOVER_TRCRY_NUMBER_S1_CLIENT, env.getProperty(FAILOVER_TRCRY_NUMBER_S2_CLIENT, "3"))));

        return clientProperties;
    }

    /**
     * 解析Common配置
     *
     * @param env
     * @return
     */
    public static CommonProperties getCommonProperties(ConfigurableEnvironment env) {
        if (null == env) {
            throw new SystemException("ConfigurableEnvironment not is null");
        }

        CommonProperties commonProperties = new CommonProperties();
        commonProperties.setTest(env.getProperty(Const.TEST_KEY));

        Map<String, Map<String, String>> registry = Maps.newHashMap();
        Map<String, String> registries = getPropertiesStartingWith(env, REGSITRY_KEY);
        registries.forEach((key, value) -> {
            // mrpc.registry[default].type
            String key_ = key.substring(key.indexOf('[') + 1, key.indexOf(']'));
            String field = key.substring(key.indexOf(']') + 2);
            Map<String, String> v = registry.getOrDefault(key_, Maps.newHashMap());
            v.put(field, value);
            registry.put(key_, v);
        });

        commonProperties.setRegistry(registry);

        Map<String, Map<String, String>> custom = Maps.newHashMap();

        Map<String, String> customs = getPropertiesStartingWith(env, CUSTOM_KEY);
        customs.forEach((key, value) -> {
            // mrpc.custom[userService].directAddress=10.50.11.221:3921
            String key_ = key.substring(key.indexOf('[') + 1, key.indexOf(']'));
            String field = key.substring(key.indexOf(']') + 2);
            Map<String, String> v = custom.getOrDefault(key_, Maps.newHashMap());
            v.put(field, value);
            custom.put(key_, v);
        });
        commonProperties.setCustom(custom);

        // netty配置读取
        Map<String, Object> nettyConfigMap = getPropertiesStartingWith(env, "mrpc.netty");

        if (CollectionUtils.isNotEmpty(nettyConfigMap)) {
            NettyConfig nettyConfig = new NettyConfig();

            int connTimeout = (Integer) nettyConfigMap.getOrDefault("mrpc.netty.connTimeout",
                    nettyConfigMap.getOrDefault("mrpc.netty.conn-timeout", 10));

            nettyConfig.setConnTimeout(connTimeout);

            int backlog = (Integer) nettyConfigMap.getOrDefault("mrpc.netty.backlog", 1024);
            nettyConfig.setBacklog(backlog);

            commonProperties.setNetty(nettyConfig);
        }
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

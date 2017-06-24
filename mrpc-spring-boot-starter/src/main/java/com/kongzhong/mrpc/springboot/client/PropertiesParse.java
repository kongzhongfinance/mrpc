package com.kongzhong.mrpc.springboot.client;

import com.google.common.collect.Maps;
import com.kongzhong.mrpc.Const;
import com.kongzhong.mrpc.config.NettyConfig;
import com.kongzhong.mrpc.enums.LbStrategyEnum;
import com.kongzhong.mrpc.exception.SystemException;
import com.kongzhong.mrpc.springboot.config.CommonProperties;
import com.kongzhong.mrpc.springboot.config.RpcClientProperties;
import com.kongzhong.mrpc.utils.CollectionUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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

        clientProperties.setAppId(env.getProperty(CLINET_APP_ID_STYLE1, env.getProperty(CLINET_APP_ID_STYLE2, "default")));

        clientProperties.setTransport(env.getProperty(TRANSPORT_CLIENT, "tcp"));

        clientProperties.setSerialize(env.getProperty(SERIALIZE_CLIENT, "kyro"));

        clientProperties.setLbStrategy(env.getProperty(LB_STRATEGY_STYLE1_CLIENT, env.getProperty(LB_STRATEGY_STYLE2_CLIENT, LbStrategyEnum.ROUND.name())));

        clientProperties.setHaStrategy(env.getProperty(LB_STRATEGY_STYLE1_CLIENT, env.getProperty(LB_STRATEGY_STYLE2_CLIENT)));

        clientProperties.setDirectAddress(env.getProperty(DIRECT_ADDRESS_STYLE1_CLIENT, env.getProperty(DIRECT_ADDRESS_STYLE2_CLIENT)));

        clientProperties.setWaitTimeout(Integer.valueOf(env.getProperty(WAIT_TIMEOUT_STYLE1_CLIENT, env.getProperty(WAIT_TIMEOUT_STYLE2_CLIENT, "10000"))));

        clientProperties.setFailOverRetry(Integer.valueOf(env.getProperty(FAILOVER_TRCRY_NUMBER_STYLE1_CLIENT, env.getProperty(FAILOVER_TRCRY_NUMBER_STYLE2_CLIENT, "3"))));

        clientProperties.setSkipBind(Boolean.valueOf(env.getProperty(SKIP_BIND_SERVICE_STYLE1_CLIENT, env.getProperty(SKIP_BIND_SERVICE_STYLE2_CLIENT, Boolean.FALSE.toString()))));

        clientProperties.setRetryInterval(Integer.valueOf(env.getProperty(RETRY_INTERVAL_STYLE1_CLIENT, env.getProperty(RETRY_INTERVAL_STYLE2_CLIENT, "3000"))));

        clientProperties.setRetryCount(Integer.valueOf(env.getProperty(RETRY_COUNT_STYLE1_CLIENT, env.getProperty(RETRY_COUNT_STYLE2_CLIENT, "10"))));

        log.debug(clientProperties.toString());
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
        Map<String, Object> nettyConfigMap = getPropertiesStartingWith(env, NETTY_CONFIG_PREFIX);
        if (CollectionUtils.isNotEmpty(nettyConfigMap)) {
            NettyConfig nettyConfig = new NettyConfig();
            Object backlog = nettyConfigMap.getOrDefault(NETTY_BACKLOG, 1024);
            nettyConfig.setBacklog(Integer.valueOf(backlog.toString()));
            commonProperties.setNetty(nettyConfig);
        }

        log.debug(commonProperties.toString());

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

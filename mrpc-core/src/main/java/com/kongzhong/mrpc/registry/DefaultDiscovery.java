package com.kongzhong.mrpc.registry;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.kongzhong.mrpc.client.Connections;
import com.kongzhong.mrpc.model.ClientBean;
import com.kongzhong.mrpc.serialize.jackson.JacksonSerialize;
import com.kongzhong.mrpc.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 默认服务发现
 *
 * @author biezhi
 *         2017/4/27
 */
@Slf4j
public class DefaultDiscovery implements ServiceDiscovery {

    public static final String DEFAULT_SWAP_NAME = "mrpc_registry_swap.lock";

    private File file = new File(DEFAULT_SWAP_NAME);

    public DefaultDiscovery() {

    }

    @Override
    public void discover(ClientBean clientBean) {
        try {
            String content = Files.toString(file, Charsets.UTF_8);
            if (StringUtils.isNotEmpty(content)) {
                List<Map<String, String>> array = JacksonSerialize.parseObject(content, List.class);
                Map<String, Set<String>> mappings = Maps.newHashMap();
                for (int i = 0, len = array.size(); i < len; i++) {
                    Map<String, String> object = array.get(i);
                    String serviceName = object.get("service");
                    String address = object.get("addr");

                    if (!mappings.containsKey(address)) {
                        mappings.put(address, Sets.newHashSet(serviceName));
                    } else {
                        mappings.get(address).add(serviceName);
                    }
                }
                Connections.me().asyncConnect(mappings);
            }
        } catch (Exception e) {
            log.error("discover fail", e);
        }
    }

    @Override
    public void stop() {

    }

}


package com.kongzhong.mrpc.registry;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.kongzhong.mrpc.cluster.Connections;
import com.kongzhong.mrpc.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public void discover() {
        try {
            String content = Files.toString(file, Charsets.UTF_8);
            if (StringUtils.isNotEmpty(content)) {
                JSONArray array = JSON.parseArray(content);
                Map<String, List<String>> mappings = Maps.newConcurrentMap();
                for (int i = 0, len = array.size(); i < len; i++) {
                    JSONObject object = array.getJSONObject(i);
                    String serviceName = object.getString("service");
                    String addr = object.getString("addr");
                    if (!mappings.containsKey(addr)) {
                        mappings.put(addr, Lists.newArrayList(serviceName));
                    } else {
                        mappings.get(addr).add(serviceName);
                    }
                }
                Connections.me().updateNodes(mappings);
            }
        } catch (Exception e) {
            log.error("discover fail", e);
        }
    }

    @Override
    public void stop() {
    }

    private String read() {
        try {
            BufferedReader bf = new BufferedReader(new FileReader(DEFAULT_SWAP_NAME));
            return bf.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String read(InputStream input) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input, "utf-8"))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }

}


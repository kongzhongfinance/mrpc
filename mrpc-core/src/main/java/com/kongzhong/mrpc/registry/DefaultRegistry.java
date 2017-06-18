package com.kongzhong.mrpc.registry;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.kongzhong.mrpc.config.ServerConfig;
import com.kongzhong.mrpc.utils.JSONUtils;
import com.kongzhong.mrpc.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 默认服务注册
 *
 * @author biezhi
 *         2017/4/27
 */
@Slf4j
public class DefaultRegistry implements ServiceRegistry {

    public static final String DEFAULT_SWAP_NAME = "mrpc_registry_swap.lock";

    private File file = new File(DEFAULT_SWAP_NAME);

    public DefaultRegistry() {
        this(true);
    }

    public DefaultRegistry(boolean append) {
        try {
            if (!append && file.exists()) {
                file.delete();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }


    @Override
    public void register(String data) {
        try {
            String content = Files.readFirstLine(file, Charsets.UTF_8);
            List<Map<String, String>> array = null;
            if (StringUtils.isEmpty(content)) {
                array = new ArrayList<>();
            } else {
                array = JSONUtils.parseObject(content, List.class);
            }
            array.add(getReg(data));
            Files.write(JSONUtils.toJSONString(array), file, Charsets.UTF_8);
        } catch (Exception e) {
            log.error("register fail", e);
        }
    }

    @Override
    public void unregister(String data) {
        try {
            if (file.exists() && file.isFile()) {
                String content = Files.readFirstLine(file, Charsets.UTF_8);
                if (StringUtils.isNotEmpty(content)) {
                    List<Map<String, String>> array = JSONUtils.parseObject(content, List.class);
                    List<Map<String, String>> newArr = new ArrayList<>();
                    for (int i = 0, len = array.size(); i < len; i++) {
                        if (!data.equals(array.get(i).get("service"))) {
                            newArr.add(array.get(i));
                        }
                    }
                    if (newArr.size() > 0) {
                        Files.write(JSONUtils.toJSONString(newArr), file, Charsets.UTF_8);
                    } else {
                        Files.write("", file, Charsets.UTF_8);
                    }
                }
            }
        } catch (Exception e) {
            log.error("unregister fail", e);
        }
    }

    private Map<String, String> getReg(String serviceName) {
        String addr = ServerConfig.me().getAddress();
        Map<String, String> obj = new HashMap<>();
        obj.put("service", serviceName);
        obj.put("addr", addr);
        return obj;
    }

}

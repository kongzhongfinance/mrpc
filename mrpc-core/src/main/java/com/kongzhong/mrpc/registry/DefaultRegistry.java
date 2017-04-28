package com.kongzhong.mrpc.registry;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.kongzhong.mrpc.config.ServerConfig;
import com.kongzhong.mrpc.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

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
            JSONArray array = null;
            if (StringUtils.isEmpty(content)) {
                array = new JSONArray();
            } else {
                array = JSON.parseArray(content);
            }
            array.add(getReg(data));

            Files.write(array.toJSONString(), file, Charsets.UTF_8);
        } catch (Exception e) {
            log.error("register fail", e);
        }
    }

    @Override
    public void unregister(String data) {
        try {
            String content = Files.readFirstLine(file, Charsets.UTF_8);
            if (StringUtils.isNotEmpty(content)) {
                JSONArray array = JSON.parseArray(content);
                JSONArray newArr = new JSONArray();
                for (int i = 0, len = array.size(); i < len; i++) {
                    if (!data.equals(array.getJSONObject(i).getString("service"))) {
                        newArr.add(array.getJSONObject(i));
                    }
                }
                if (newArr.size() > 0) {
                    Files.write(newArr.toJSONString(), file, Charsets.UTF_8);
                } else {
                    Files.write("", file, Charsets.UTF_8);
                }
            }
        } catch (Exception e) {
            log.error("register fail", e);
        }
    }

    private JSONObject getReg(String serviceName) {
        String host = ServerConfig.me().getHost();
        int port = ServerConfig.me().getPort();
        JSONObject obj = new JSONObject();
        obj.put("service", serviceName);
        obj.put("addr", host + ":" + port);
        return obj;
    }

}

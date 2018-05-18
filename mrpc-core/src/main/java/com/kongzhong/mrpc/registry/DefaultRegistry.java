package com.kongzhong.mrpc.registry;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.kongzhong.mrpc.model.ServiceBean;
import com.kongzhong.mrpc.serialize.jackson.JacksonSerialize;
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
        this(false);
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
    public boolean register(ServiceBean serviceBean) {
        try {
            String data = serviceBean.getServiceName();
            String address = serviceBean.getAddress();

            String content = Files.readFirstLine(file, Charsets.UTF_8);
            List<Map<String, String>> array = null;
            if (StringUtils.isEmpty(content)) {
                array = new ArrayList<>();
            } else {
                array = JacksonSerialize.parseObject(content, List.class);
            }
            array.add(this.getReg(address, data));
            Files.write(JacksonSerialize.toJSONString(array), file, Charsets.UTF_8);
            return true;
        } catch (Exception e) {
            log.error("register fail", e);
            return false;
        }
    }

    @Override
    public void unRegister(ServiceBean serviceBean) {
        try {
            String data = serviceBean.getServiceName();
            if (file.exists() && file.isFile()) {
                String content = Files.readFirstLine(file, Charsets.UTF_8);
                if (StringUtils.isNotEmpty(content)) {
                    List<Map<String, String>> array = JacksonSerialize.parseObject(content, List.class);
                    List<Map<String, String>> newArr = new ArrayList<>();
                    for (int i = 0, len = array.size(); i < len; i++) {
                        if (!data.equals(array.get(i).get("service"))) {
                            newArr.add(array.get(i));
                        }
                    }
                    if (newArr.size() > 0) {
                        Files.write(JacksonSerialize.toJSONString(newArr), file, Charsets.UTF_8);
                    } else {
                        Files.write("", file, Charsets.UTF_8);
                    }
                }
            }
        } catch (Exception e) {
            log.error("unRegister fail", e);
        }
    }

    private Map<String, String> getReg(String address, String serviceName) {
        Map<String, String> obj = new HashMap<>();
        obj.put("service", serviceName);
        obj.put("address", address);
        return obj;
    }

}

package com.kongzhong.mrpc.server;

import com.kongzhong.mrpc.interceptor.RpcInteceptor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * RPC映射关系存储
 *
 * @author biezhi
 *         2017/4/24
 */
@Data
@NoArgsConstructor
public class RpcMapping {

    private Map<String, Object> handlerMap;

    private List<RpcInteceptor> inteceptors;

    private static final class RpcMappingHolder {
        private static final RpcMapping $ = new RpcMapping();
    }

    public static RpcMapping me() {
        return RpcMappingHolder.$;
    }

}

package com.kongzhong.mrpc.admin.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author biezhi
 * @date 2018/6/11
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServerMind {

    private String  id;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isroot;
    private String  topic;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String  parentid;
}

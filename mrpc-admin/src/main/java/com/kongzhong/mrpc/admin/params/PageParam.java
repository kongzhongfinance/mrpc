package com.kongzhong.mrpc.admin.params;

import lombok.Data;

/**
 * @author biezhi
 * @date 2018/6/8
 */
@Data
public class PageParam {

    private Integer page = 1;
    private Integer limit = 10;

}

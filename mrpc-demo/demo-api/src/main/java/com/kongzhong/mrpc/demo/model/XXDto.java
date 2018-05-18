package com.kongzhong.mrpc.demo.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author biezhi
 * @date 2017/9/13
 */
@Data
public class XXDto {

    private Integer    age;
    private BigDecimal money;
    private Date       birthday;
}

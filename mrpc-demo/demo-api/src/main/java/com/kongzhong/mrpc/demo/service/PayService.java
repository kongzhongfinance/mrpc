package com.kongzhong.mrpc.demo.service;

import com.kongzhong.mrpc.demo.model.NoConstructor;

import java.math.BigDecimal;

/**
 * @author biezhi
 *         2017/4/28
 */
public interface PayService {

    String pay(String msg, BigDecimal money);

    BigDecimal getMoney(Double money);

    /**
     * 参数没有构造函数
     *
     * @param noConstructor
     */
    NoConstructor noConstructor(NoConstructor noConstructor);

}

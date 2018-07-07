package com.kongzhong.mrpc.demo.service;

import com.kongzhong.mrpc.annotation.Comment;
import com.kongzhong.mrpc.demo.model.NoConstructor;

import java.math.BigDecimal;

/**
 * @author biezhi
 *         2017/4/28
 */
@Comment(name = "支付服务", owners = "biezhi", emails = "biezhi.me@gmail.com")
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

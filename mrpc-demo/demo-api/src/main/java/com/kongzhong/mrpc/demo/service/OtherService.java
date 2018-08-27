package com.kongzhong.mrpc.demo.service;

import com.kongzhong.mrpc.annotation.Command;
import com.kongzhong.mrpc.annotation.Comment;

/**
 * @author biezhi
 * @date 2017/11/22
 */
@Comment(name = "其他服务", owners = "biezhi", emails = "biezhi.me@gmail.com")
public interface OtherService {

    @Command(waitTimeout = 2000)
    String waitTime(int seconds);

}

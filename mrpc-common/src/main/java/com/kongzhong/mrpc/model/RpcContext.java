package com.kongzhong.mrpc.model;

import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import lombok.ToString;

/**
 * @author biezhi
 *         2017/4/24
 */
@Data
@ToString
public class RpcContext {

    private ChannelHandlerContext ctx;

    public RpcContext(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

}

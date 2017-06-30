package com.kongzhong.mrpc.transport.netty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles a server-side channel.
 */
@Sharable
public class TelnetServerHandler extends SimpleChannelInboundHandler<String> {

    private static final Set<String> LOCAL_SERVICES = new HashSet<>();

    public static void addService(String service) {
        LOCAL_SERVICES.add(service);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Send greeting for a new connection.
        ctx.write("\r\n");
        ctx.write("===============================\r\n");
        ctx.write("🌝    欢迎使用空中金融 mrpc \r\n");
        ctx.write("===============================\r\n");
        ctx.write("service: 显示当前节点服务列表\r\n");
        ctx.write("quit\t: 退出该死的telnet程序\r\n");
        ctx.write("===============================\r\n");
        ctx.write("🐥 🐥 🐥 🐥 🐥 🐥 🐥 🐥 🐥 🐥 🐥 🐥 🐥 🐥 🐥\r\n\r\n");
        ctx.write("\uD83D\uDC49 ");
        ctx.flush();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String request) throws Exception {
        // Generate and write a response.
        StringBuffer response = new StringBuffer("\uD83D\uDC49  ");
        boolean close = false;

        if (request.isEmpty()) {
            response = new StringBuffer("大佬按下了回车\r\n");
            response.append("\uD83D\uDC49  ");
        } else if ("quit".equals(request.toLowerCase())) {
            response = new StringBuffer("大佬再见 👋\r\n");
            close = true;
        } else {
            switch (request) {
                case "service":

                    String list = LOCAL_SERVICES.stream()
                            .map(val -> "- " + val + "\r\n")
                            .collect(Collectors.joining());

                    response = new StringBuffer("\r\n");
                    response.append(list).append("\r\n");
                    response.append("\uD83D\uDC49  ");
                    break;
                default:
                    break;
            }
        }

        // We do not need to write a ChannelBuffer here.
        // We know the encoder inserted at TelnetPipelineFactory will do the conversion.
        ChannelFuture future = ctx.write(response);

        // Close the connection after sending 'Have a good day!'
        // if the client has sent 'bye'.
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

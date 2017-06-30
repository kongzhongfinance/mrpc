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

    private static final String PREFIX = "# ";

    private static final String START_BANNER =
            "\r\n" +
                    "==============================================\r\n" +
                    "  Welcome to use the kongzhong finance mrpc \r\n" +
                    "==============================================\r\n" +
                    "  service : show current node service list\r\n" +
                    "  quit    : exit telnet application\r\n" +
                    "==============================================\r\n" +
                    "\r\n" + PREFIX;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Send greeting for a new connection.
        ctx.write(START_BANNER);
        ctx.flush();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String request) throws Exception {
        // Generate and write a response.
        String response = PREFIX;
        boolean close = false;

        if (request.isEmpty()) {
        } else if ("quit".equals(request.toLowerCase()) || "q".equals(request.toLowerCase())) {
            response = "\r\nBye";
            close = true;
        } else {
            switch (request) {
                case "service":
                    String list = LOCAL_SERVICES.stream()
                            .map(val -> "- " + val + "\r\n")
                            .collect(Collectors.joining());

                    response = "\r\n" + list + "\r\n" + PREFIX;
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

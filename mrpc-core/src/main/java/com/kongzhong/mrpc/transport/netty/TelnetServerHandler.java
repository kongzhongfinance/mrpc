package com.kongzhong.mrpc.transport.netty;

import com.kongzhong.mrpc.server.RpcMapping;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.stream.Collectors;

/**
 * Handles a server-side channel.
 */
@Sharable
public class TelnetServerHandler extends SimpleChannelInboundHandler<String> {

    /**
     * terminal前缀
     */
    private static final String PREFIX = "# ";

    /**
     * 服务端配置信息
     */
    private static String serverConfig = "";

    /**
     * 连接到服务端banner
     */
    private static final String START_BANNER =
            "\r\n" +
                    "==============================================\r\n" +
                    "  Welcome to use the kongzhong finance mrpc \r\n" +
                    "==============================================\r\n" +
                    "  service : show current node service list\r\n" +
                    "  config  : show current node start config\r\n" +
                    "  quit    : exit telnet application\r\n" +
                    "==============================================\r\n" +
                    "\r\n" + PREFIX;

    public static void setServerConfig(String config) {
        serverConfig = config;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(START_BANNER);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String request) throws Exception {
        // Generate and write a response.
        StringBuffer response = new StringBuffer(PREFIX);
        boolean close = false;

        if (request.isEmpty()) {
        } else if ("quit".equals(request.toLowerCase()) || "q".equals(request.toLowerCase())) {
            response.append("\r\nBye\r\n");
            close = true;
        } else {
            switch (request) {
                case "service":

                    String list = RpcMapping.me().getServiceBeanMap().values().stream()
                            .map(bean -> bean.getServiceName())
                            .map(val -> "- " + val + "\r\n")
                            .collect(Collectors.joining());

                    response.append("\r\n");
                    response.append(list);
                    response.append("\r\n");
                    response.append(PREFIX);
                    break;
                case "config":
                    response.append(serverConfig);
                    response.append("\r\n");
                    response.append(PREFIX);
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

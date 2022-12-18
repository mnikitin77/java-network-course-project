package org.mnikitin.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.mnikitin.service.MessageValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageHandler extends SimpleChannelInboundHandler<String> {

    private static final Logger log = LoggerFactory.getLogger(MessageHandler.class);
    private static final String OK = "2\nok";
    private static final String ERROR = "3\nerr";
    private final MessageValidator validator = new MessageValidator();

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("Connected client: {}", ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) {
        var response = validator.validate(s) ? OK : ERROR;
        log.info("Sending response to client [{}]:\n{}", ctx, response);
        ctx.writeAndFlush(response);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Closing connection for client {} due to exception: {}", ctx, cause);
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("Client {} closed connection", ctx);
    }
}

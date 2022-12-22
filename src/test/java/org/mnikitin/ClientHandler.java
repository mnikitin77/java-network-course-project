package org.mnikitin;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class ClientHandler  extends SimpleChannelInboundHandler<String> {

    private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);
    private final Consumer<String> responseProcessor;

    public ClientHandler(Consumer<String> responseProcessor) {
        this.responseProcessor = responseProcessor;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("Channel is active");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) {
        log.info("Received response from server:\n{}", s);
        responseProcessor.accept(s);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Closing connection due to exception: ", cause);
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }
}

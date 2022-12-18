package org.mnikitin;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

public class SimpleTextProtocolServerTest {

    private static final int PORT = 12345;
    private static final String HOST = "127.0.0.1";
    private static final String OK_RESPONSE = "2\nok";
    private static final String ERROR_RESPONSE = "3\nerr";
    private static final SimpleTextProtocolServer server = new SimpleTextProtocolServer();

    @BeforeAll
    static void init() {
        new Thread(() -> server.start(PORT)).start();
    }

    @AfterAll
    static void clean() {
        server.stop();
    }

    @Test
    public void whenSend5hello_then2ok() throws InterruptedException {
        var message = "5\nhello";
        sendMessageAndValidateResponse(
                message,
                (s) -> Assertions.assertThat(s).isEqualTo(OK_RESPONSE)
        );
    }

    @Test
    public void whenSend4cool_then2ok() throws InterruptedException {
        var message = "4\ncool";
        sendMessageAndValidateResponse(
                message,
                (s) -> Assertions.assertThat(s).isEqualTo(OK_RESPONSE)
        );
    }

    @Test
    public void whenSend6haha_then3err() throws InterruptedException {
        var message = "6\nhaha";
        sendMessageAndValidateResponse(
                message,
                (s) -> Assertions.assertThat(s).isEqualTo(ERROR_RESPONSE)
        );
    }

    private void sendMessageAndValidateResponse(
            String message,
            Consumer<String> responseProcessor
    ) throws InterruptedException {

        var worker = new NioEventLoopGroup();
        try {
            var b = new Bootstrap();
            b.group(worker)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    new StringDecoder(),
                                    new StringEncoder(),
                                    new ClientHandler(responseProcessor)
                            );
                        }
                    });

            var channel = b.connect(HOST, PORT).sync().channel();
            channel.writeAndFlush(message);
            channel.closeFuture().sync();
        } finally {
            worker.shutdownGracefully();
        }
    }
}
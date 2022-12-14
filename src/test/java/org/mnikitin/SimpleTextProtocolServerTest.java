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
import org.mnikitin.configuration.SslContextConfiguration;

import java.util.function.Consumer;

public class SimpleTextProtocolServerTest {

    private static final int PORT = 12345;
    private static final String HOST = "127.0.0.1";
    private static final String OK_RESPONSE = "2\nok";
    private static final String ERROR_RESPONSE = "3\nerr";
    private static final String KEYSTORE_PATH = "/ssl/clientkeystore.jks";
    private static final String TRUSTSTORE_PATH = "/ssl/clienttruststore.jks";
    private static final String PASSWORD = "qwerty";


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
    public void whenSend5hello_then2ok() throws Exception {
        sendMessageAndValidateResponse(
                "5\nhello",
                (s) -> Assertions.assertThat(s).isEqualTo(OK_RESPONSE)
        );
    }

    @Test
    public void whenSend4cool_then2ok() throws Exception {
        sendMessageAndValidateResponse(
                "4\ncool",
                (s) -> Assertions.assertThat(s).isEqualTo(OK_RESPONSE)
        );
    }

    @Test
    public void whenSend6haha_then3err() throws Exception {
        sendMessageAndValidateResponse(
                "6\nhaha",
                (s) -> Assertions.assertThat(s).isEqualTo(ERROR_RESPONSE)
        );
    }

    private void sendMessageAndValidateResponse(
            String message,
            Consumer<String> responseProcessor
    ) throws Exception {

        var sslCtx = new SslContextConfiguration(
                KEYSTORE_PATH, PASSWORD, TRUSTSTORE_PATH, PASSWORD
        ).getClientSslContext();

        var worker = new NioEventLoopGroup();
        try {
            var b = new Bootstrap();
            b.group(worker)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    sslCtx.newHandler(ch.alloc()),
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
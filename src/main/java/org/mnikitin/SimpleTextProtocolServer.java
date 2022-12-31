package org.mnikitin;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslHandler;
import org.mnikitin.configuration.Configuration;
import org.mnikitin.configuration.SslContextConfiguration;
import org.mnikitin.handler.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class SimpleTextProtocolServer {

    private static final Logger log = LoggerFactory.getLogger(SimpleTextProtocolServer.class);
    private static final String KEYSTORE_PATH = "/ssl/serverkeystore.jks";
    private static final String TRUSTSTORE_PATH = "/ssl/servertruststore.jks";
    private static final String PASSWORD = "qwerty";

    private Channel channel;

    public static void main(String[] args) {
        var port = args.length > 0
                ? Integer.parseInt(args[0])
                : Configuration.instance().port();
        new SimpleTextProtocolServer().start(port);
    }

    public void start(int port) {
        var bossGroup = new NioEventLoopGroup();
        var workerGroup = new NioEventLoopGroup();

        try {
            var b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {

                            var sslEngine = new SslContextConfiguration(
                                    KEYSTORE_PATH, PASSWORD, TRUSTSTORE_PATH, PASSWORD
                            ).getServerSslContext().newEngine(ch.alloc());
                            sslEngine.setUseClientMode(false);
                            sslEngine.setNeedClientAuth(true);

                            ch.pipeline().addLast(
                                    new SslHandler(sslEngine),
                                    new StringDecoder(),
                                    new StringEncoder(),
                                    new MessageHandler()
                            );
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            channel = b.bind(port).sync().channel();
            log.info("SimpleProtocolServer started on port {}.", port);
            channel.closeFuture().sync();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void stop() {
        Objects.requireNonNull(channel);
        channel.close();
    }
}

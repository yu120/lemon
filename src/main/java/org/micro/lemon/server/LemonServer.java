package org.micro.lemon.server;

import org.micro.lemon.common.LemonConfig;
import org.micro.lemon.filter.LemonFactory;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadFactory;

/**
 * LemonServer
 *
 * @author lry
 */
@Slf4j
public class LemonServer {

    private Channel channel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private LemonChannelInitializer channelInitializer;

    /**
     * The initialize
     */
    public void initialize() {
        LemonConfig lemonConfig = LemonConfig.loadConfig();
        LemonFactory.INSTANCE.initialize(lemonConfig);
        log.info("The starting open server by config:{}", lemonConfig);

        ThreadFactory ioThreadFactory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("lemon-io").build();
        ThreadFactory workThreadFactory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("lemon-work").build();
        this.channelInitializer = new LemonChannelInitializer(lemonConfig);

        try {
            this.bossGroup = new NioEventLoopGroup(lemonConfig.getIoThread(), ioThreadFactory);
            this.workerGroup = new NioEventLoopGroup(lemonConfig.getWorkThread(), workThreadFactory);
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(channelInitializer);

            ChannelFuture channelFuture = serverBootstrap.bind(lemonConfig.getPort()).sync();
            channelFuture.addListener((future) -> log.info("The start server is success"));
            this.channel = channelFuture.channel();

            Runtime.getRuntime().addShutdownHook(new Thread(LemonServer.this::destroy));
            if (lemonConfig.isServer()) {
                channel.closeFuture().sync();
            }
        } catch (Exception e) {
            log.error("The start server is fail", e);
        }
    }

    /**
     * The destroy
     */
    public void destroy() {
        log.info("The starting close server...");

        try {
            if (channel != null) {
                channel.close();
            }
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
            if (channelInitializer != null) {
                channelInitializer.destroy();
            }
            LemonFactory.INSTANCE.destroy();
        } catch (Exception e) {
            log.error("The destroy server is fail", e);
        }
    }

}

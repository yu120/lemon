package cn.micro.lemon.server;

import cn.micro.lemon.LemonConfig;
import cn.micro.lemon.filter.LemonChain;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.net.URL;

/**
 * Lemon Server by Netty
 *
 * @author lry
 */
@Slf4j
public class LemonServer {

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public LemonServer() {
        LemonConfig lemonConfig = loadConfig();
        LemonChain.initialize(lemonConfig);

        ThreadFactoryBuilder ioBuilder = new ThreadFactoryBuilder();
        ioBuilder.setDaemon(true);
        ioBuilder.setNameFormat("lemon-biz");
        ThreadFactoryBuilder workBuilder = new ThreadFactoryBuilder();
        workBuilder.setDaemon(true);
        workBuilder.setNameFormat("lemon-work");

        try {
            this.bossGroup = new NioEventLoopGroup(lemonConfig.getIoThread(), ioBuilder.build());
            this.workerGroup = new NioEventLoopGroup(lemonConfig.getWorkThread(), workBuilder.build());
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new HttpRequestDecoder());
                            ch.pipeline().addLast(new HttpResponseEncoder());
                            // Convert multiple requests from HTTP to FullHttpRequest/FullHttpResponse
                            ch.pipeline().addLast(new HttpObjectAggregator(Integer.MAX_VALUE));
                            ch.pipeline().addLast(new LemonServerHandler(lemonConfig));
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(lemonConfig.getPort()).sync();

            Runtime.getRuntime().addShutdownHook(new Thread(LemonServer.this::destroy));
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("The start server is fail", e);
        }
    }

    /**
     * The load config
     *
     * @return
     */
    private LemonConfig loadConfig() {
        URL url = this.getClass().getClassLoader().getResource("lemon.yml");
        if (url != null) {
            try {
                return new Yaml().loadAs(new FileInputStream(url.getFile()), LemonConfig.class);
            } catch (Exception e) {
                throw new RuntimeException("The load as yaml is exception", e);
            }
        }

        throw new RuntimeException("Not found lemon.yml");
    }

    /**
     * The destroy
     */
    public void destroy() {
        try {
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
        } catch (Exception e) {
            log.error("The destroy server is fail", e);
        }
    }

}

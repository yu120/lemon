package org.micro.lemon.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.registry.RegistryFactory;
import org.apache.dubbo.registry.RegistryService;
import org.micro.lemon.common.LemonConfig;
import org.micro.lemon.common.config.BizTaskConfig;
import org.micro.lemon.common.utils.NetUtils;
import org.micro.lemon.common.utils.StandardThreadExecutor;
import org.micro.lemon.filter.LemonFactory;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lemon Server by Netty
 *
 * @author lry
 */
@Slf4j
public class LemonServer {

    private Channel channel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private StandardThreadExecutor standardThreadExecutor;

    private URL serverUrl;
    public static RegistryService registryService = null;

    /**
     * The initialize
     */
    public void initialize() {
        LemonConfig lemonConfig = loadConfig();
        LemonFactory.INSTANCE.initialize(lemonConfig);
        log.info("The starting open server by config:{}", lemonConfig);

        URL url = URL.valueOf(lemonConfig.getRegistryAddress());
        RegistryFactory registryFactory = ExtensionLoader.getExtensionLoader(RegistryFactory.class).getExtension(url.getProtocol());
        registryService = registryFactory.getRegistry(url);

        ThreadFactoryBuilder ioBuilder = new ThreadFactoryBuilder();
        ioBuilder.setDaemon(true);
        ioBuilder.setNameFormat("lemon-io");
        ThreadFactoryBuilder workBuilder = new ThreadFactoryBuilder();
        workBuilder.setDaemon(true);
        workBuilder.setNameFormat("lemon-work");

        BizTaskConfig bizTaskConfig = lemonConfig.getBiz();
        if (bizTaskConfig.getCoreThread() > 0) {
            ThreadFactoryBuilder bizBuilder = new ThreadFactoryBuilder();
            bizBuilder.setDaemon(true);
            bizBuilder.setNameFormat("lemon-biz");
            this.standardThreadExecutor = new StandardThreadExecutor(
                    bizTaskConfig.getCoreThread(),
                    bizTaskConfig.getMaxThread(),
                    bizTaskConfig.getKeepAliveTime(),
                    TimeUnit.MILLISECONDS,
                    bizTaskConfig.getQueueCapacity(),
                    bizBuilder.build(),
                    bizTaskConfig.getRejectedStrategy().getHandler());
            standardThreadExecutor.prestartAllCoreThreads();
        }

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
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("http-decoder", new HttpRequestDecoder());
                            // Convert multiple requests from HTTP to FullHttpRequest/FullHttpResponse
                            pipeline.addLast("http-aggregator", new HttpObjectAggregator(lemonConfig.getMaxContentLength()));
                            pipeline.addLast("http-encoder", new HttpResponseEncoder());
                            pipeline.addLast("http-chunked", new ChunkedWriteHandler());
                            pipeline.addLast("serverHandler", new LemonServerHandler(lemonConfig, standardThreadExecutor));
                        }
                    });

            ChannelFuture channelFuture = serverBootstrap.bind(lemonConfig.getPort()).sync();
            channelFuture.addListener((future) -> log.info("The start server is success"));
            this.channel = channelFuture.channel();

            this.serverUrl = new URL("http", NetUtils.getLocalHost(), lemonConfig.getPort(), lemonConfig.getApplication());
            //registryService.register(serverUrl);

            Runtime.getRuntime().addShutdownHook(new Thread(LemonServer.this::destroy));
            if (lemonConfig.isServer()) {
                channel.closeFuture().sync();
            }
        } catch (Exception e) {
            log.error("The start server is fail", e);
        }
    }

    /**
     * The load config
     *
     * @return {@link LemonConfig}
     */
    private LemonConfig loadConfig() {
        java.net.URL url = this.getClass().getClassLoader().getResource("lemon.yml");
        if (url != null) {
            try {
                Iterable<Object> iterable = new Yaml().loadAll(new FileInputStream(url.getFile()));
                for (Object object : iterable) {
                    JSON json = recursion(object);
                    return json.toJavaObject(LemonConfig.class);
                }
            } catch (Exception e) {
                throw new RuntimeException("The load as yaml is exception", e);
            }
        }

        throw new RuntimeException("Not found lemon.yml");
    }

    private static Pattern linePattern = Pattern.compile("-(\\w)");

    /**
     * 下划线转驼峰
     */
    public static String lineToHump(String str) {
        str = str.toLowerCase();
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private JSON recursion(Object object) {
        if (object instanceof Map) {
            JSONObject jsonObject = new JSONObject();
            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) object).entrySet()) {
                String key = String.valueOf(entry.getKey());
                if (key.startsWith("-D") || key.startsWith("-d")) {
                    key = key.substring(2);
                } else if (key.contains("-")) {
                    key = lineToHump(key);
                }
                if (entry.getValue() instanceof Map || entry.getValue() instanceof Collection) {
                    jsonObject.put(key, recursion(entry.getValue()));
                } else {
                    jsonObject.put(key, entry.getValue());
                }
            }

            return jsonObject;
        } else if (object instanceof Collection) {
            JSONArray jsonArray = new JSONArray();
            for (Object tempObject : (List<Object>) object) {
                if (tempObject instanceof Map || tempObject instanceof Collection) {
                    jsonArray.add(recursion(tempObject));
                } else {
                    jsonArray.add(tempObject);
                }
            }

            return jsonArray;
        } else {
            throw new RuntimeException("未知数据类型" + object);
        }
    }

    /**
     * The destroy
     */
    public void destroy() {
        log.info("The starting close server...");

        try {
            registryService.unregister(serverUrl);

            if (channel != null) {
                channel.close();
            }
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
            if (standardThreadExecutor != null) {
                standardThreadExecutor.shutdown();
            }

            LemonFactory.INSTANCE.destroy();
        } catch (Exception e) {
            log.error("The destroy server is fail", e);
        }
    }

}

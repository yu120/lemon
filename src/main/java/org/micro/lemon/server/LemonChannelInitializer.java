package org.micro.lemon.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.micro.lemon.common.LemonConfig;
import org.micro.lemon.common.config.BizTaskConfig;
import org.micro.lemon.common.utils.StandardThreadExecutor;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * LemonChannelInitializer
 *
 * @author lry
 */
public class LemonChannelInitializer extends ChannelInitializer<SocketChannel> {

    private LemonConfig lemonConfig;
    private LemonServerHandler lemonServerHandler;
    private StandardThreadExecutor standardThreadExecutor;

    public LemonChannelInitializer(LemonConfig lemonConfig) {
        // create biz thread pool
        BizTaskConfig bizTaskConfig = lemonConfig.getBiz();
        if (bizTaskConfig.getCoreThread() > 0) {
            ThreadFactory bizThreadFactory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("lemon-biz").build();
            this.standardThreadExecutor = new StandardThreadExecutor(
                    bizTaskConfig.getCoreThread(), bizTaskConfig.getMaxThread(),
                    bizTaskConfig.getKeepAliveTime(), TimeUnit.MILLISECONDS, bizTaskConfig.getQueueCapacity(),
                    bizThreadFactory, bizTaskConfig.getRejectedStrategy().getHandler());
            standardThreadExecutor.prestartAllCoreThreads();
        }

        this.lemonConfig = lemonConfig;
        this.lemonServerHandler = new LemonServerHandler(lemonConfig, standardThreadExecutor);
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("http-decoder", new HttpRequestDecoder());
        // Convert multiple requests from HTTP to FullHttpRequest/FullHttpResponse
        pipeline.addLast("http-aggregator", new HttpObjectAggregator(lemonConfig.getMaxContentLength()));
        pipeline.addLast("http-encoder", new HttpResponseEncoder());
        pipeline.addLast("http-chunked", new ChunkedWriteHandler());
        pipeline.addLast("serverHandler", lemonServerHandler);
    }

    public void destroy() {
        if (standardThreadExecutor != null) {
            standardThreadExecutor.shutdown();
        }
    }

}

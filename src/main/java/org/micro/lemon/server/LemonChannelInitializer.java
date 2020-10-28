package org.micro.lemon.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.micro.lemon.common.LemonConfig;

/**
 * LemonChannelInitializer
 *
 * @author lry
 */
public class LemonChannelInitializer extends ChannelInitializer<SocketChannel> {

    private LemonConfig lemonConfig;
    private LemonServerHandler lemonServerHandler;

    public LemonChannelInitializer(LemonConfig lemonConfig) {
        this.lemonConfig = lemonConfig;
        this.lemonServerHandler = new LemonServerHandler(lemonConfig);
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
        if (lemonServerHandler != null) {
            lemonServerHandler.destroy();
        }
    }

}

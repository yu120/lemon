package cn.micro.lemon.server;

import cn.micro.lemon.LemonConfig;
import cn.micro.lemon.LemonStatusCode;
import cn.micro.lemon.filter.LemonChain;
import cn.micro.lemon.filter.LemonContext;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.micro.neural.common.thread.StandardThreadExecutor;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Lemon Server Handler
 *
 * @author lry
 */
public class LemonServerHandler extends ChannelInboundHandlerAdapter {

    private LemonConfig lemonConfig;
    private StandardThreadExecutor standardThreadExecutor = null;

    public LemonServerHandler(LemonConfig lemonConfig) {
        this.lemonConfig = lemonConfig;
        if (lemonConfig.getBizCoreThread() > 0) {
            ThreadFactoryBuilder bizBuilder = new ThreadFactoryBuilder();
            bizBuilder.setDaemon(true);
            bizBuilder.setNameFormat("lemon-biz");
            this.standardThreadExecutor = new StandardThreadExecutor(
                    lemonConfig.getBizCoreThread(),
                    lemonConfig.getBizMaxThread(),
                    lemonConfig.getBizKeepAliveTime(),
                    TimeUnit.MILLISECONDS,
                    lemonConfig.getBizQueueCapacity(),
                    bizBuilder.build());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            LemonContext lemonContext = new LemonContext(lemonConfig, ctx);

            FullHttpRequest request = (FullHttpRequest) msg;
            String uri = request.uri();
            if (!uri.startsWith("/" + lemonConfig.getApplication() + "/")) {
                lemonContext.writeAndFlush(LemonStatusCode.NO_HANDLER_FOUND_EXCEPTION);
                return;
            }

            wrapperChainContext(lemonContext, ctx, request);
            if (standardThreadExecutor == null) {
                try {
                    LemonChain.processor(lemonContext);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            } else {
                standardThreadExecutor.execute(() -> {
                    try {
                        LemonChain.processor(lemonContext);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                });
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    private void wrapperChainContext(LemonContext lemonContext, ChannelHandlerContext ctx, FullHttpRequest request) {
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        HttpHeaders httpHeaders = request.headers();

        lemonContext.setUri(request.uri());
        lemonContext.setPath(decoder.path());
        lemonContext.setMethod(request.method().name());
        lemonContext.setKeepAlive(HttpUtil.isKeepAlive(request));

        int contentLength;
        byte[] contentByte;
        ByteBuf byteBuf = null;
        try {
            byteBuf = request.content();
            contentLength = byteBuf.readableBytes();
            contentByte = new byte[contentLength];
            byteBuf.readBytes(contentByte);
        } finally {
            if (byteBuf != null) {
                byteBuf.release();
            }
        }

        lemonContext.setContentLength(contentLength);
        lemonContext.setContentByte(contentByte);
        lemonContext.setContent(new String(contentByte, StandardCharsets.UTF_8));

        lemonContext.addPaths(decoder.path());
        lemonContext.getHeaderAll().addAll(httpHeaders.entries());
        lemonContext.getParameterAll().putAll(decoder.parameters());
        lemonContext.addHeaders(httpHeaders.entries());
        lemonContext.addParameters(decoder.parameters());
    }

}
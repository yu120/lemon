package cn.micro.lemon.server;

import cn.micro.lemon.LemonConfig;
import cn.micro.lemon.LemonStatusCode;
import cn.micro.lemon.filter.LemonChain;
import cn.micro.lemon.filter.LemonContext;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.QueryStringDecoder;
import lombok.extern.slf4j.Slf4j;
import org.micro.neural.common.thread.StandardThreadExecutor;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Lemon Server Handler
 *
 * @author lry
 */
@Slf4j
public class LemonServerHandler extends ChannelInboundHandlerAdapter {

    private LemonConfig lemonConfig;
    private ConcurrentMap<String, Channel> channels;
    private StandardThreadExecutor standardThreadExecutor = null;

    public LemonServerHandler(LemonConfig lemonConfig) {
        this.lemonConfig = lemonConfig;
        this.channels = new ConcurrentHashMap<>(lemonConfig.getMaxChannel());
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
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        if (channels.size() >= lemonConfig.getMaxChannel()) {
            // Direct close connection beyond maximum connection limit
            log.warn("The connected channel size out of limit: limit={} current={}", lemonConfig.getMaxChannel(), channels.size());
            channel.close();
        } else {
            String channelKey = getChannelKey((InetSocketAddress) channel.localAddress(), (InetSocketAddress) channel.remoteAddress());
            channels.put(channelKey, channel);
            ctx.fireChannelRegistered();
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
                    log.error(t.getMessage(), t);
                    lemonContext.writeAndFlush(LemonStatusCode.MICRO_ERROR_EXCEPTION);
                }
            } else {
                standardThreadExecutor.execute(() -> {
                    try {
                        LemonChain.processor(lemonContext);
                    } catch (Throwable t) {
                        log.error(t.getMessage(), t);
                        lemonContext.writeAndFlush(LemonStatusCode.MICRO_ERROR_EXCEPTION);
                    }
                });
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("The handler exception caught: " + cause.getMessage(), cause);
        if (ctx != null) {
            ctx.close();
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        String channelKey = getChannelKey((InetSocketAddress) channel.localAddress(), (InetSocketAddress) channel.remoteAddress());
        channels.remove(channelKey);
        ctx.fireChannelUnregistered();
    }

    public Map<String, Channel> getChannels() {
        return channels;
    }

    private void wrapperChainContext(LemonContext lemonContext, ChannelHandlerContext ctx, FullHttpRequest request) {
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        HttpHeaders httpHeaders = request.headers();

        // use header Lemon-Id as lemon id
        String lemonId = httpHeaders.get(LemonContext.LEMON_ID);
        if (lemonId != null && lemonId.length() > 0) {
            lemonContext.setId(lemonId);
        }

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

    /**
     * key = remote address + local address
     *
     * @param local  {@link InetSocketAddress}
     * @param remote {@link InetSocketAddress}
     * @return channel key
     */
    private String getChannelKey(InetSocketAddress local, InetSocketAddress remote) {
        String key = "";
        if (local == null || local.getAddress() == null) {
            key += "null-";
        } else {
            key += local.getAddress().getHostAddress() + ":" + local.getPort() + "-";
        }

        if (remote == null || remote.getAddress() == null) {
            key += "null";
        } else {
            key += remote.getAddress().getHostAddress() + ":" + remote.getPort();
        }

        return key;
    }

}
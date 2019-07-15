package cn.micro.lemon.server;

import cn.micro.lemon.common.LemonConfig;
import cn.micro.lemon.common.LemonStatusCode;
import cn.micro.lemon.filter.LemonChain;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;
import org.micro.neural.common.collection.MapEntry;
import org.micro.neural.common.thread.StandardThreadExecutor;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.RejectedExecutionException;

/**
 * Lemon Server Handler
 *
 * @author lry
 */
@Slf4j
public class LemonServerHandler extends ChannelInboundHandlerAdapter {

    private LemonConfig lemonConfig;
    private ConcurrentMap<String, Channel> channels;
    private StandardThreadExecutor standardThreadExecutor;

    public LemonServerHandler(LemonConfig lemonConfig, StandardThreadExecutor standardThreadExecutor) {
        this.lemonConfig = lemonConfig;
        this.standardThreadExecutor = standardThreadExecutor;
        this.channels = new ConcurrentHashMap<>(lemonConfig.getMaxChannel());
    }

    public Map<String, Channel> getChannels() {
        return channels;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        if (channels.size() >= lemonConfig.getMaxChannel()) {
            // Direct close connection beyond maximum connection limit
            log.warn("The connected channel size out of limit: limit={} current={}", lemonConfig.getMaxChannel(), channels.size());
            channel.close();
        } else {
            String channelKey = getChannelKey(channel.localAddress(), channel.remoteAddress());
            channels.put(channelKey, channel);
            ctx.fireChannelRegistered();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            LemonContext lemonContext = new LemonContext(lemonConfig, ctx);
            wrapperChainContext(lemonContext, (FullHttpRequest) msg);
            if (!lemonContext.getUri().startsWith(LemonContext.URL_DELIMITER +
                    lemonConfig.getApplication() + LemonContext.URL_DELIMITER)) {
                lemonContext.writeAndFlush(LemonStatusCode.NOT_FOUND);
                return;
            }

            if (standardThreadExecutor == null) {
                try {
                    LemonChain.processor(lemonContext);
                } catch (Throwable t) {
                    log.error(t.getMessage(), t);
                    lemonContext.writeAndFlush(LemonStatusCode.INTERNAL_SERVER_ERROR);
                }
            } else {
                try {
                    standardThreadExecutor.execute(() -> {
                        try {
                            LemonChain.processor(lemonContext);
                        } catch (Throwable t) {
                            log.error(t.getMessage(), t);
                            lemonContext.writeAndFlush(LemonStatusCode.INTERNAL_SERVER_ERROR);
                        }
                    });
                } catch (RejectedExecutionException e) {
                    lemonContext.writeAndFlush(LemonStatusCode.TOO_MANY_REQUESTS);
                }
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
        String channelKey = getChannelKey(channel.localAddress(), channel.remoteAddress());
        channels.remove(channelKey);
        ctx.fireChannelUnregistered();
    }

    private void wrapperChainContext(LemonContext lemonContext, FullHttpRequest request) {
        String uri = request.uri();
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        HttpHeaders httpHeaders = request.headers();

        // use header Lemon-Id as lemon id
        String lemonId = httpHeaders.get(LemonContext.LEMON_ID);
        if (lemonId != null && lemonId.length() > 0) {
            lemonContext.setId(lemonId);
        } else {
            lemonContext.getHeaders().put(LemonContext.LEMON_ID, lemonContext.getId());
            lemonContext.getHeaderAll().add(new MapEntry<>(LemonContext.LEMON_ID, lemonContext.getId()));
        }

        lemonContext.setUri(request.uri());
        lemonContext.setPath(decoder.path());
        lemonContext.setMethod(request.method().name());
        lemonContext.setKeepAlive(HttpUtil.isKeepAlive(request));

        String applicationPath = LemonContext.URL_DELIMITER + lemonConfig.getApplication();
        if (uri.startsWith(applicationPath)) {
            lemonContext.setContextPath(request.uri().substring(applicationPath.length()));
        }

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
     * key = local address + remote address
     *
     * @param localSocketAddress  {@link SocketAddress}
     * @param remoteSocketAddress {@link SocketAddress}
     * @return channel key
     */
    private String getChannelKey(SocketAddress localSocketAddress, SocketAddress remoteSocketAddress) {
        InetSocketAddress local = (InetSocketAddress) localSocketAddress;
        InetSocketAddress remote = (InetSocketAddress) remoteSocketAddress;

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
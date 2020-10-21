package org.micro.lemon.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;
import org.micro.lemon.common.LemonConfig;
import org.micro.lemon.common.LemonStatusCode;
import org.micro.lemon.common.utils.StandardThreadExecutor;
import org.micro.lemon.filter.LemonChain;
import org.slf4j.MDC;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/**
 * LemonServerHandler
 *
 * @author lry
 */
@Slf4j
@ChannelHandler.Sharable
public class LemonServerHandler extends ChannelInboundHandlerAdapter {

    private LemonConfig lemonConfig;
    private ConcurrentMap<String, Channel> channels;
    private StandardThreadExecutor standardThreadExecutor;

    public LemonServerHandler(LemonConfig lemonConfig, StandardThreadExecutor standardThreadExecutor) {
        this.lemonConfig = lemonConfig;
        this.standardThreadExecutor = standardThreadExecutor;
        this.channels = new ConcurrentHashMap<>(lemonConfig.getMaxConnection());
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        if (channels.size() > lemonConfig.getMaxConnection()) {
            // Direct close connection beyond maximum connection limit
            log.warn("The connected channel size out of limit: limit={} current={}", lemonConfig.getMaxConnection(), channels.size());
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
            LemonContext lemonContext = this.buildContext(ctx, (FullHttpRequest) msg);
            if (!lemonContext.getRequest().getApplicationPath().equals(LemonContext.URL_DELIMITER +
                    lemonConfig.getApplication() + LemonContext.URL_DELIMITER)) {
                lemonContext.callback(LemonStatusCode.NOT_FOUND);
                return;
            }

            if (standardThreadExecutor == null) {
                try {
                    new LemonChain(lemonContext);
                } catch (Throwable t) {
                    log.error(t.getMessage(), t);
                    lemonContext.callback(LemonStatusCode.INTERNAL_SERVER_ERROR);
                }
            } else {
                try {
                    standardThreadExecutor.execute(() -> {
                        try {
                            new LemonChain(lemonContext);
                        } catch (Throwable t) {
                            log.error(t.getMessage(), t);
                            lemonContext.callback(LemonStatusCode.INTERNAL_SERVER_ERROR);
                        }
                    });
                } catch (RejectedExecutionException e) {
                    log.error(e.getMessage(), e);
                    lemonContext.callback(LemonStatusCode.TOO_MANY_REQUESTS);
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel channel = ctx.channel();
        String channelKey = getChannelKey(channel.localAddress(), channel.remoteAddress());
        log.error("The handler channel[" + channelKey + "] exception caught: " + cause.getMessage(), cause);
        if (channel.isOpen()) {
            channel.close();
        }
        if (!ctx.isRemoved()) {
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

    /**
     * The wrapper chain context
     *
     * @param ctx     {@link ChannelHandlerContext}
     * @param request {@link FullHttpRequest}
     */
    private LemonContext buildContext(ChannelHandlerContext ctx, FullHttpRequest request) {
        // read headers
        HttpHeaders httpHeaders = request.headers();
        String requestId = httpHeaders.get(LemonContext.LEMON_ID_KEY,
                UUID.randomUUID().toString().replace("-", ""));
        MDC.put(LemonContext.LEMON_ID_KEY, requestId);
        Map<String, List<String>> originHeaders = new HashMap<>();
        for (Map.Entry<String, String> entry : httpHeaders.entries()) {
            originHeaders.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(entry.getValue());
        }

        // read uri
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        String path = decoder.path();

        // read content
        int contentLength;
        byte[] content;
        ByteBuf byteBuf = null;
        try {
            byteBuf = request.content();
            contentLength = byteBuf.readableBytes();
            content = new byte[contentLength];
            byteBuf.readBytes(content);
        } finally {
            if (byteBuf != null) {
                byteBuf.release();
            }
        }

        final Map<String, Object> headers = new HashMap<>(originHeaders);
        headers.putAll(decoder.parameters());
        headers.put(LemonContext.LEMON_ID_KEY, requestId);
        headers.put(LemonContext.URI_KEY, request.uri());
        headers.put(LemonContext.APP_PATH_KEY, path.substring(0, path.indexOf(LemonContext.URL_DELIMITER, 1) - 1));
        headers.put(LemonContext.CONTEXT_PATH_KEY, path.substring(path.indexOf(LemonContext.URL_DELIMITER, 1)));
        headers.put(LemonContext.PATH_KEY, path);
        headers.put(LemonContext.METHOD_KEY, request.method().name());
        headers.put(LemonContext.KEEP_ALIVE_KEY, HttpUtil.isKeepAlive(request));
        headers.put(LemonContext.CONTENT_LENGTH_KEY, contentLength);
        return new LemonContext(new LemonRequest(headers, content)) {
            @Override
            public void callback(LemonStatusCode statusCode, String message, Object body) {
                FullHttpResponse response = buildRespone(statusCode, message, body);
                ctx.writeAndFlush(response).addListener(future -> MDC.remove(LemonContext.LEMON_ID_KEY));
            }
        };
    }

    /**
     * The write and flush
     *
     * @param statusCode {@link LemonStatusCode}
     * @param message    custom message
     * @param body       response body content
     */
    private FullHttpResponse buildRespone(LemonStatusCode statusCode, String message, Object body) {
        ByteBuf byteBuf;
        if (body == null) {
            byteBuf = Unpooled.buffer(0);
        } else if (body instanceof ByteBuf) {
            byteBuf = (ByteBuf) body;
        } else if (body instanceof byte[]) {
            byteBuf = Unpooled.wrappedBuffer((byte[]) body);
        } else {
            byteBuf = Unpooled.wrappedBuffer(String.valueOf(body).getBytes(StandardCharsets.UTF_8));
        }

        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, byteBuf);
        response.headers().set(LemonContext.LEMON_CODE_KEY, statusCode.getCode());
        response.headers().set(LemonContext.LEMON_CODE_MESSAGE,
                (message != null && message.trim().length() > 0) ? message : statusCode.getMessage());
        response.headers().set(com.google.common.net.HttpHeaders.CONTENT_LENGTH,
                (response.content() == null ? 0 : response.content().readableBytes()));
        response.headers().set(com.google.common.net.HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        response.headers().set(com.google.common.net.HttpHeaders.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        response.headers().set(com.google.common.net.HttpHeaders.ACCEPT_ENCODING, HttpHeaderValues.GZIP_DEFLATE);
        return response;
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

        String key;
        if (remote == null || remote.getAddress() == null) {
            key = "unknown->";
        } else {
            key = remote.getAddress().getHostAddress() + ":" + remote.getPort() + "->";
        }

        if (local == null || local.getAddress() == null) {
            key += "unknown";
        } else {
            key += local.getAddress().getHostAddress() + ":" + local.getPort();
        }

        return key;
    }

}
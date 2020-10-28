package org.micro.lemon.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
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
import org.micro.lemon.common.config.BizTaskConfig;
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

    public LemonServerHandler(LemonConfig lemonConfig) {
        this.lemonConfig = lemonConfig;
        this.channels = new ConcurrentHashMap<>(lemonConfig.getMaxConnection());

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
            // build context
            final LemonContext lemonContext = new LemonContext() {
                @Override
                public void callback(LemonStatusCode statusCode, String message) {
                    FullHttpResponse response = buildResponse(statusCode, message, getResponse());
                    ctx.writeAndFlush(response).addListener(future -> MDC.remove(LemonContext.LEMON_ID_KEY));
                }
            };

            // check application uri
            final FullHttpRequest request = (FullHttpRequest) msg;
            if (!request.uri().startsWith(LemonContext.URL_DELIMITER + lemonConfig.getApplication() + LemonContext.URL_DELIMITER)) {
                lemonContext.callback(LemonStatusCode.NOT_FOUND);
                return;
            }

            // wrapper request context
            this.wrapperRequest(lemonContext, request);

            // submit request task
            if (standardThreadExecutor == null) {
                new LemonChain(lemonContext).start0();
            } else {
                try {
                    standardThreadExecutor.submit(() -> new LemonChain(lemonContext).start0());
                } catch (RejectedExecutionException e) {
                    log.error(LemonStatusCode.TOO_MANY_REQUESTS.getMessage(), e);
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

    public void destroy() {
        if (standardThreadExecutor != null) {
            standardThreadExecutor.shutdown();
        }
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

    /**
     * The wrapper chain context
     *
     * @param lemonContext {@link LemonContext}
     * @param request      {@link FullHttpRequest}
     */
    private void wrapperRequest(LemonContext lemonContext, FullHttpRequest request) {
        Map<String, List<String>> originHeaders = new HashMap<>();

        // read headers
        HttpHeaders httpHeaders = request.headers();
        String requestId = httpHeaders.get(LemonContext.LEMON_ID_KEY, UUID.randomUUID().toString().replace("-", ""));
        MDC.put(LemonContext.LEMON_ID_KEY, requestId);
        for (Map.Entry<String, String> entry : httpHeaders.entries()) {
            if (!lemonConfig.getIgnoreHeaders().contains(entry.getKey())) {
                originHeaders.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(entry.getValue());
            }
        }

        // read uri
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        String path = decoder.path();
        originHeaders.putAll(decoder.parameters());

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

        // build context
        final Map<String, Object> headers = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : originHeaders.entrySet()) {
            headers.put(entry.getKey(), entry.getValue().size() == 1 ? entry.getValue().get(0) : entry.getValue());
        }
        headers.put(LemonContext.LEMON_ID_KEY, requestId);
        headers.put(LemonContext.URI_KEY, request.uri());
        headers.put(LemonContext.CLIENT_HOST_KEY, httpHeaders.contains("X-Forwarded-For") ?
                httpHeaders.get("X-Forwarded-For") : httpHeaders.get("Host"));
        headers.put(LemonContext.APP_PATH_KEY, path.substring(0, path.indexOf(LemonContext.URL_DELIMITER, 1)));
        headers.put(LemonContext.CONTEXT_PATH_KEY, path.substring(path.indexOf(LemonContext.URL_DELIMITER, 1)));
        headers.put(LemonContext.PATH_KEY, path);
        headers.put(LemonContext.METHOD_KEY, request.method().name());
        headers.put(LemonContext.KEEP_ALIVE_KEY, HttpUtil.isKeepAlive(request));
        headers.put(LemonContext.CONTENT_LENGTH_KEY, contentLength);
        lemonContext.getRequest().addHeader(headers);
        lemonContext.getRequest().setContent(content);
    }

    /**
     * The write and flush
     *
     * @param statusCode {@link LemonStatusCode}
     * @param message    custom message
     * @param response   {@link LemonResponse}
     */
    private FullHttpResponse buildResponse(LemonStatusCode statusCode, String message, LemonResponse response) {
        // build content
        ByteBuf byteBuf;
        if (response.getContent() == null) {
            byteBuf = Unpooled.buffer(0);
        } else if (response.getContent() instanceof ByteBuf) {
            byteBuf = (ByteBuf) response.getContent();
        } else if (response.getContent() instanceof byte[]) {
            byteBuf = Unpooled.wrappedBuffer((byte[]) response.getContent());
        } else {
            byteBuf = Unpooled.wrappedBuffer(String.valueOf(response.getContent()).getBytes(StandardCharsets.UTF_8));
        }

        // build http response
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, byteBuf);
        for (Map.Entry<String, Object> entry : response.getHeaders().entrySet()) {
            httpResponse.headers().set(entry.getKey(), entry.getValue());
        }

        // build response customize header
        httpResponse.headers().set(LemonContext.LEMON_CODE_KEY, statusCode.getCode());
        if (!response.getHeaders().containsKey(com.google.common.net.HttpHeaders.CONTENT_TYPE)) {
            httpResponse.headers().set(com.google.common.net.HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        }

        // build response fixed header
        httpResponse.headers().set(com.google.common.net.HttpHeaders.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        httpResponse.headers().set(com.google.common.net.HttpHeaders.ACCEPT_ENCODING, HttpHeaderValues.GZIP_DEFLATE);
        httpResponse.headers().set(LemonContext.LEMON_CODE_MESSAGE, (message != null && message.trim().length() > 0) ? message : statusCode.getMessage());
        httpResponse.headers().set(com.google.common.net.HttpHeaders.CONTENT_LENGTH, (httpResponse.content() == null ? 0 : httpResponse.content().readableBytes()));
        return httpResponse;
    }

}
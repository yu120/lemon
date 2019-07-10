package cn.micro.lemon.server;

import cn.micro.lemon.filter.LemonChain;
import cn.micro.lemon.filter.LemonContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;

import java.nio.charset.StandardCharsets;

/**
 * Lemon Server Handler
 *
 * @author lry
 */
public class LemonServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            LemonContext lemonContext = buildChainContext(ctx, request);
            new LemonChain().doFilter(lemonContext);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    private LemonContext buildChainContext(ChannelHandlerContext ctx, FullHttpRequest request) {
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        HttpHeaders httpHeaders = request.headers();

        LemonContext.LemonContextBuilder builder = LemonContext.builder();
        builder.uri(request.uri());
        builder.path(decoder.path());
        builder.method(request.method().name());
        builder.keepAlive(HttpUtil.isKeepAlive(request));

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

        builder.contentLength(contentLength);
        builder.contentByte(contentByte);
        builder.content(new String(contentByte, StandardCharsets.UTF_8));

        LemonContext lemonContext = builder.build();
        lemonContext.setCtx(ctx);
        lemonContext.getHeaderAll().addAll(httpHeaders.entries());
        lemonContext.getParameterAll().putAll(decoder.parameters());
        lemonContext.addHeaders(httpHeaders.entries());
        lemonContext.addParameters(decoder.parameters());
        return lemonContext;
    }

}
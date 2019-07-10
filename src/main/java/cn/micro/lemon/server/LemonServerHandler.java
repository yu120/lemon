package cn.micro.lemon.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;

import java.nio.charset.StandardCharsets;

public class LemonServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest httpRequest = (FullHttpRequest) msg;

            ByteBuf byteBuf = httpRequest.content();
            byte[] contentByte = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(contentByte);
            String content = new String(contentByte, StandardCharsets.UTF_8);
            System.out.println("Server接受的客户端的信息 :" + content);

            String contentStr = "这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容" +
                    "这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容" +
                    "这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容" +
                    "这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容" +
                    "这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容" +
                    "这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容" +
                    "这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容" +
                    "这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容" +
                    "这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容" +
                    "这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容" +
                    "这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容" +
                    "这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容" +
                    "这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容" +
                    "这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容" +
                    "这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容" +
                    "这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容" +
                    "这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容" +
                    "这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容" +
                    "这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容" +
                    "这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容" +
                    "这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容" +
                    "这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容" +
                    "这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容" +
                    "这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容" +
                    "这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容" +
                    "这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容" +
                    "这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容" +
                    "这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容这是响应体内容";

            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK, Unpooled.wrappedBuffer(contentStr.getBytes(StandardCharsets.UTF_8)));
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain;charset=UTF-8");
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            ctx.writeAndFlush(response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

}
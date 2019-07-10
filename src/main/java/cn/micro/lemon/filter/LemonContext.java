package cn.micro.lemon.filter;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lemon Context
 *
 * @author lry
 */
@Data
@Builder
@ToString
public class LemonContext {

    private final static String APPLICATION_JSON = "application/json;charset=UTF-8";

    private String path;
    private String uri;
    private String method;
    private boolean keepAlive;
    private int contentLength;

    private final Map<String, String> headers = new HashMap<>();
    private final List<Map.Entry<String, String>> headerAll = new ArrayList<>();
    private final Map<String, String> parameters = new HashMap<>();
    private final Map<String, List<String>> parameterAll = new HashMap<>();

    private byte[] contentByte;
    private String content;
    private ChannelHandlerContext ctx;

    public void addParameters(Map<String, List<String>> parameterAll) {
        if (parameterAll == null || parameterAll.size() == 0) {
            return;
        }

        for (Map.Entry<String, List<String>> entry : parameterAll.entrySet()) {
            List<String> values = entry.getValue();
            if (values == null || values.size() == 0) {
                continue;
            }
            parameters.put(entry.getKey(), values.get(0));
        }
    }

    public void addHeaders(List<Map.Entry<String, String>> headersAll) {
        if (headersAll == null || headersAll.size() == 0) {
            return;
        }
        for (Map.Entry<String, String> entry : headersAll) {
            headers.put(entry.getKey(), entry.getValue());
        }
    }

    public void writeAndFlush(Object obj) {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(JSON.toJSONString(obj).getBytes(StandardCharsets.UTF_8));
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, byteBuf);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, APPLICATION_JSON);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        ctx.writeAndFlush(response);
    }

}

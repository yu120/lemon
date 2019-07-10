package cn.micro.lemon.server;

import cn.micro.lemon.LemonInvoke;
import cn.micro.lemon.dubbo.ServiceDefinition;
import cn.micro.lemon.filter.LemonContext;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import org.apache.dubbo.common.constants.CommonConstants;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Lemon Server Handler
 *
 * @author lry
 */
public class LemonServerHandler extends ChannelInboundHandlerAdapter {

    private final static String APPLICATION_JSON = "application/json;charset=UTF-8";

    private LemonInvoke lemonInvoke;

    public LemonServerHandler(LemonInvoke lemonInvoke) {
        this.lemonInvoke = lemonInvoke;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;

            LemonContext lemonContext = buildChainContext(ctx, request);

//            CompletableFuture<Object> future = lemonInvoke.invokeAsync(serviceDefinition);
//            future.whenComplete((result, t) -> {
//                lemonContext.writeAndFlush(result);
//            });
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    private LemonContext buildChainContext(ChannelHandlerContext ctx, FullHttpRequest request) {
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        HttpHeaders httpHeaders = request.headers();

        LemonContext.ChainContextBuilder builder = LemonContext.builder();
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

    private ServiceDefinition buildServiceDefinition(FullHttpRequest httpRequest) {
        QueryStringDecoder decoder = new QueryStringDecoder(httpRequest.uri());
        String uri = decoder.path();
        if (uri.startsWith("/")) {
            uri = uri.substring(1);
        }
        if (uri.endsWith("/")) {
            uri = uri.substring(0, uri.length() - 1);
        }

        String[] pathArray = uri.split("/");
        if (pathArray.length != 3) {
            return null;
        }

        ServiceDefinition serviceDefinition = new ServiceDefinition();
        serviceDefinition.setApplication(pathArray[0]);
        serviceDefinition.setService(pathArray[1]);
        serviceDefinition.setMethod(pathArray[2]);

        Map<String, List<String>> parameters = decoder.parameters();
        if (parameters.containsKey(CommonConstants.GROUP_KEY)) {
            List<String> values = parameters.get(CommonConstants.GROUP_KEY);
            if (values != null && values.size() > 0) {
                serviceDefinition.setGroup(values.get(0));
            }
        }
        if (parameters.containsKey(CommonConstants.VERSION_KEY)) {
            List<String> values = parameters.get(CommonConstants.VERSION_KEY);
            if (values != null && values.size() > 0) {
                serviceDefinition.setVersion(values.get(0));
            }
        }

        ByteBuf byteBuf = httpRequest.content();
        byte[] contentByte = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(contentByte);
        String content = new String(contentByte, StandardCharsets.UTF_8);

        List<Object> paramValues = new ArrayList<>();
        if (JSON.isValid(content)) {
            Object object = JSON.parse(content);
            if (object instanceof JSONArray) {
                paramValues.addAll(((JSONArray) object).toJavaList(Map.class));
            } else {
                paramValues.add(object);
            }
        } else {
            paramValues.add(content);
        }

        serviceDefinition.setParamValues(paramValues.toArray(new Object[0]));
        return serviceDefinition;
    }

}
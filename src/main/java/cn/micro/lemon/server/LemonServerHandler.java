package cn.micro.lemon.server;

import cn.micro.lemon.LemonInvoke;
import cn.micro.lemon.MicroConfig;
import cn.micro.lemon.dubbo.DubboConfig;
import cn.micro.lemon.dubbo.DubboLemonInvoke;
import cn.micro.lemon.dubbo.ServiceDefinition;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import org.apache.dubbo.common.constants.CommonConstants;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LemonServerHandler extends ChannelInboundHandlerAdapter {

    private LemonInvoke lemonInvoke;

    public LemonServerHandler() {
        this.lemonInvoke = new DubboLemonInvoke();

        DubboConfig dubboConfig = new DubboConfig();
        dubboConfig.setRegistryAddress("zookeeper://127.0.0.1:2181");
        dubboConfig.setMetadataAddress("zookeeper://127.0.0.1:2181");

        MicroConfig microConfig = new MicroConfig();
        microConfig.setApplication("micro-dubbo-gateway");
        microConfig.setDubbo(dubboConfig);
        lemonInvoke.initialize(microConfig);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest httpRequest = (FullHttpRequest) msg;
            ServiceDefinition serviceDefinition = buildServiceDefinition(httpRequest);
            Object result = lemonInvoke.invoke(serviceDefinition);

            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK, Unpooled.wrappedBuffer(JSON.toJSONString(result).getBytes(StandardCharsets.UTF_8)));
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

    private Object toObject(byte[] bytes) {
        Object obj = null;

        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            obj = ois.readObject();
            ois.close();
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return obj;
    }

}
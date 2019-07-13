package cn.micro.lemon.http;

import cn.micro.lemon.LemonConfig;
import cn.micro.lemon.LemonInvoke;
import cn.micro.lemon.LemonStatusCode;
import cn.micro.lemon.ServiceMapping;
import cn.micro.lemon.filter.LemonContext;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.micro.neural.common.micro.AntPathMatcher;
import org.micro.neural.extension.Extension;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Jsoup Lemon Invoke
 *
 * @author lry
 */
@Slf4j
@Extension("jsoup")
public class JsoupLemonInvoke implements LemonInvoke {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final ConcurrentMap<String, ServiceMapping> mappings = new ConcurrentHashMap<>();

    @Override
    public void initialize(LemonConfig lemonConfig) {
        List<ServiceMapping> services = lemonConfig.getServices();
        for (ServiceMapping serviceMapping : services) {
            mappings.put(serviceMapping.getService(), serviceMapping);
        }
    }

    @Override
    public Object invoke(LemonContext context) {
        ServiceMapping mapping = matchMapping(context.getContextPath());
        if (mapping == null) {
            context.writeAndFlush(LemonStatusCode.NOT_FOUND);
            return null;
        }

        Connection connection = Jsoup.connect(mapping.getUrl());
        Connection.Request request = connection.request();
        request.method(buildMethod(context.getMethod()));
        try {
            Connection.Response response = connection.execute();
            context.getResHeaders().putAll(response.headers());
            return response.bodyAsBytes();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<Object> invokeAsync(LemonContext context) {
        Object object = invoke(context);
        if (object instanceof CompletableFuture) {
            return (CompletableFuture<Object>) object;
        }

        return CompletableFuture.completedFuture(object);
    }

    @Override
    public LemonStatusCode failure(LemonContext context, Throwable throwable) {
        return LemonStatusCode.CALL_ORIGINAL_UNKNOWN;
    }

    @Override
    public void destroy() {

    }

    private ServiceMapping matchMapping(String uri) {
        for (ConcurrentMap.Entry<String, ServiceMapping> entry : mappings.entrySet()) {
            if (antPathMatcher.match(entry.getKey(), uri)) {
                return entry.getValue();
            }
        }

        return null;
    }

    private Connection.Method buildMethod(String method) {
        switch (method) {
            case "POST":
                return Connection.Method.POST;
            case "PUT":
                return Connection.Method.PUT;
            case "DELETE":
                return Connection.Method.DELETE;
            case "PATCH":
                return Connection.Method.PATCH;
            case "HEAD":
                return Connection.Method.HEAD;
            case "OPTIONS":
                return Connection.Method.OPTIONS;
            case "TRACE":
                return Connection.Method.TRACE;
            default:
                return Connection.Method.GET;
        }
    }

}
package cn.micro.lemon.http;

import cn.micro.lemon.common.LemonConfig;
import cn.micro.lemon.common.LemonInvoke;
import cn.micro.lemon.common.LemonStatusCode;
import cn.micro.lemon.common.ServiceMapping;
import cn.micro.lemon.server.LemonContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.micro.neural.common.micro.AntPathMatcher;
import org.micro.neural.extension.Extension;

import java.io.IOException;
import java.util.List;
import java.util.Map;
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

    private LemonConfig lemonConfig;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final ConcurrentMap<String, ServiceMapping> mappings = new ConcurrentHashMap<>();

    @Override
    public void initialize(LemonConfig lemonConfig) {
        this.lemonConfig = lemonConfig;
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

        String originalUrl = mapping.getUrl();
        if (mapping.isFullUrl()) {
            originalUrl += context.getContextPath();
        } else {
            String servicePrefix = mapping.getService();
            servicePrefix = servicePrefix.substring(0, servicePrefix.lastIndexOf("/"));
            originalUrl += context.getContextPath().substring(servicePrefix.length());
        }

        Connection connection = Jsoup.connect(originalUrl);
        Connection.Request request = connection.request();
        request.method(ConnectionMethod.valueOf(context.getMethod()).getMethod());
        for (Map.Entry<String, String> entry : context.getHeaderAll()) {
            request.header(entry.getKey(), entry.getValue());
        }
        if (!(context.getContent() == null || context.getContent().length() == 0)) {
            request.requestBody(context.getContent());
        }

        // setter timeout(ms)
        Long timeout = mapping.getTimeout();
        if (timeout == null) {
            timeout = lemonConfig.getOriginalTimeout();
        }
        if (timeout > 0) {
            request.timeout(timeout.intValue());
        }

        try {
            Connection.Response response = connection.execute();
            context.getResHeaders().putAll(response.headers());
            context.getResHeaders().put(LemonContext.CALL_CODE, response.statusCode());
            context.getResHeaders().put(LemonContext.CALL_MESSAGE, response.statusMessage());
            return response.bodyAsBytes();
        } catch (IOException e) {
            context.getResHeaders().put(LemonContext.CALL_MESSAGE, e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
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

    @Getter
    @AllArgsConstructor
    public enum ConnectionMethod {

        // ====

        GET(Connection.Method.GET),
        POST(Connection.Method.POST),
        PUT(Connection.Method.PUT),
        DELETE(Connection.Method.DELETE),
        PATCH(Connection.Method.PATCH),
        HEAD(Connection.Method.HEAD),
        OPTIONS(Connection.Method.OPTIONS),
        TRACE(Connection.Method.TRACE);

        private Connection.Method method;

    }

}
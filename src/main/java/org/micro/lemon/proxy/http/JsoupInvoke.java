package org.micro.lemon.proxy.http;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.micro.lemon.common.LemonConfig;
import org.micro.lemon.common.LemonInvoke;
import org.micro.lemon.common.LemonStatusCode;
import org.micro.lemon.common.ServiceMapping;
import org.micro.lemon.common.utils.AntPathMatcher;
import org.micro.lemon.extension.Extension;
import org.micro.lemon.server.LemonContext;
import org.micro.lemon.server.LemonRequest;
import org.micro.lemon.server.LemonResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Jsoup Lemon Invoke
 *
 * @author lry
 */
@Slf4j
@Extension("jsoup")
public class JsoupInvoke implements LemonInvoke {

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
    public LemonContext invoke(LemonContext context) {
        LemonRequest request = context.getRequest();
        ServiceMapping mapping = null;
        for (ConcurrentMap.Entry<String, ServiceMapping> entry : mappings.entrySet()) {
            if (antPathMatcher.match(entry.getKey(), request.getContextPath())) {
                mapping = entry.getValue();
            }
        }
        if (mapping == null) {
            context.onCallback(LemonStatusCode.NOT_FOUND);
            return null;
        }

        String originalUrl = mapping.getUrl();
        if (mapping.isFullUrl()) {
            originalUrl += request.getContextPath();
        } else {
            String servicePrefix = mapping.getService();
            servicePrefix = servicePrefix.substring(0, servicePrefix.lastIndexOf("/"));
            originalUrl += request.getContextPath().substring(servicePrefix.length());
        }

        Connection connection = Jsoup.connect(originalUrl);
        Connection.Request sendRequest = connection.request();
        sendRequest.method(ConnectionMethod.valueOf(request.getHttpMethod()).getMethod());
        for (Map.Entry<String, Object> entry : request.getHeaders().entrySet()) {
            sendRequest.header(entry.getKey(), String.valueOf(entry.getValue()));
        }
        byte[] bytes = (byte[]) request.getContent();
        if (request.getContent() != null && bytes.length > 0) {
            sendRequest.requestBody(new String(bytes, StandardCharsets.UTF_8));
        }

        // setter timeout(ms)
        Long timeout = mapping.getTimeout();
        if (timeout == null) {
            timeout = lemonConfig.getOriginal().getTimeout();
        }
        if (timeout > 0) {
            sendRequest.timeout(timeout.intValue());
        }

        try {
            Connection.Response response = connection.execute();
            Map<String, Object> headers = new HashMap<>(response.headers());
            context.setResponse(new LemonResponse(headers, response.bodyAsBytes()));
            return context;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public LemonStatusCode failure(LemonContext context, Throwable throwable) {
        return LemonStatusCode.CALL_ORIGINAL_UNKNOWN;
    }

    @Override
    public void destroy() {

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
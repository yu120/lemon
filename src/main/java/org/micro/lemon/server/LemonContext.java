package org.micro.lemon.server;

import org.micro.lemon.common.LemonStatusCode;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Lemon Context
 *
 * @author lry
 */
@Data
@Slf4j
@ToString
public class LemonContext implements MessageCallback {

    public final static String LEMON_ID_KEY = "X-Lemon-Id";
    public final static String URI_KEY = "X-Lemon-Uri";
    public final static String APP_PATH_KEY = "X-Lemon-ApplicationPath";
    public final static String CONTEXT_PATH_KEY = "X-Lemon-ContextPath";
    public final static String PATH_KEY = "X-Lemon-Path";
    public final static String METHOD_KEY = "X-Lemon-HttpMethod";
    public final static String KEEP_ALIVE_KEY = "X-Lemon-KeepAlive";
    public final static String CONTENT_LENGTH_KEY = "X-Lemon-ContentLength";

    public final static String LEMON_CODE_KEY = "X-Lemon-Code";
    public final static String LEMON_CODE_MESSAGE = "X-Lemon-Message";
    private final static String APPLICATION_JSON = "application/json;charset=UTF-8";

    public final static String URL_DELIMITER = "/";
    public final static String HEADER_PREFIX = "X-";
    private final static String LEMON_TIME = "X-Lemon-Time";
    private final static String LEMON_PROTOCOL = "X-Lemon-Protocol";
    public final static String CALL_CODE = "X-Call-Code";
    public final static String CALL_MESSAGE = "X-Call-Message";
    public final static String INVALIDATE_CACHE = "X-Invalidate-Cache";
    public final static String LEMON_TOKEN = "X-Lemon-Token";

    private Object content;
    private final Map<String, Object> headers = new HashMap<>();

    public LemonContext(Map<String, Object> headers, Object content) {
        this.headers.putAll(headers);
        this.content = content;
    }

    @Override
    public void callback(LemonStatusCode statusCode, String message, Object body) {

    }

    public String getHeaderValue(String headerKey) {
        return headers.containsKey(headerKey) ? String.valueOf(headers.get(headerKey)) : null;
    }

    public String getUri() {
        return this.getHeaderValue(URI_KEY);
    }

    public String getApplicationPath() {
        return this.getHeaderValue(APP_PATH_KEY);
    }

    public String getContextPath() {
        return this.getHeaderValue(CONTEXT_PATH_KEY);
    }

    public String getHttpMethod() {
        return this.getHeaderValue(METHOD_KEY);
    }

    public String getPath() {
        return this.getHeaderValue(PATH_KEY);
    }

    public void onCallback(LemonStatusCode statusCode) {
        callback(statusCode, statusCode.getMessage(), null);
    }

    public void onCallback(LemonStatusCode statusCode, String message) {
        callback(statusCode, message, null);
    }

    public void onCallback(LemonStatusCode statusCode, String message, Object body) {
        callback(statusCode, message, body);
    }

}

package org.micro.lemon.server;

import org.micro.lemon.common.LemonStatusCode;
import org.micro.lemon.common.ServiceMapping;
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
public class LemonContext {

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

    private final Map<String, Object> headers = new HashMap<>();
    private byte[] content;
    private MessageCallback callback;


    private Object result;
    private final Map<String, Object> resHeaders = new HashMap<>();

    private ServiceMapping serviceMapping;

    public LemonContext(Map<String, Object> headers, byte[] content, MessageCallback callback) {
        this.headers.putAll(headers);
        this.content = content;
        this.callback = callback;
    }

    public String getUri() {
        return headers.containsKey(URI_KEY) ? String.valueOf(headers.get(URI_KEY)) : null;
    }

    public String getApplicationPath() {
        return headers.containsKey(APP_PATH_KEY) ? String.valueOf(headers.get(APP_PATH_KEY)) : null;
    }

    public String getContextPath() {
        return headers.containsKey(CONTEXT_PATH_KEY) ? String.valueOf(headers.get(CONTEXT_PATH_KEY)) : null;
    }

    public String getHttpMethod() {
        return headers.containsKey(METHOD_KEY) ? String.valueOf(headers.get(METHOD_KEY)) : null;
    }

    public String getPath() {
        return headers.containsKey(PATH_KEY) ? String.valueOf(headers.get(PATH_KEY)) : null;
    }

    public List<String> getPaths() {
        return Arrays.asList(getPath().split(URL_DELIMITER));
    }

    public void onCallback(LemonStatusCode statusCode) {
        callback.callback(statusCode, null, null);
    }

    public void onCallback(LemonStatusCode statusCode, String message) {
        callback.callback(statusCode, message, null);
    }

    public void onCallback(LemonStatusCode statusCode, String message, Object body) {
        callback.callback(statusCode, message, body);
    }

}

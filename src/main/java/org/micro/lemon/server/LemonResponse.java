package org.micro.lemon.server;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * LemonResponse
 *
 * @author lry
 */
@Data
@Slf4j
@ToString
public class LemonResponse implements Serializable {

    private Object content;
    private final Map<String, Object> headers = new HashMap<>();

    public LemonResponse(Map<String, Object> headers, Object content) {
        this.headers.putAll(headers);
        this.content = content;
    }

    public String getHeaderValue(String headerKey) {
        return headers.containsKey(headerKey) ? String.valueOf(headers.get(headerKey)) : null;
    }

}

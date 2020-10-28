package org.micro.lemon.server;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * LemonResponse
 *
 * @author lry
 */
@Data
@ToString
@NoArgsConstructor
public class LemonResponse implements Serializable {

    private Object content;
    private final Map<String, Object> headers = new HashMap<>();

    public LemonResponse(Map<String, Object> headers, Object content) {
        this.headers.putAll(headers);
        this.content = content;
    }

    public void addHeader(Map<String, Object> headers) {
        this.headers.putAll(headers);
    }

    public String getHeaderValue(String headerKey) {
        return headers.containsKey(headerKey) ? String.valueOf(headers.get(headerKey)) : null;
    }

}

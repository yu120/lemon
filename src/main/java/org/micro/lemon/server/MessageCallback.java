package org.micro.lemon.server;

import org.micro.lemon.common.LemonStatusCode;

/**
 * MessageCallback
 *
 * @author lry
 */
public interface MessageCallback {

    /**
     * The callback
     *
     * @param statusCode {@link LemonStatusCode}
     * @param message    message
     * @param body       body
     */
    default void callback(LemonStatusCode statusCode, String message, Object body) {

    }

}

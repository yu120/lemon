package org.micro.lemon.server;

import org.micro.lemon.common.LemonStatusCode;

/**
 * LemonCallback
 *
 * @author lry
 */
public interface LemonCallback {

    /**
     * The callback
     *
     * @param statusCode {@link LemonStatusCode}
     */
    default void callback(LemonStatusCode statusCode) {
        callback(statusCode, statusCode.getMessage(), null);
    }

    /**
     * The callback
     *
     * @param statusCode {@link LemonStatusCode}
     * @param message    message
     */
    default void callback(LemonStatusCode statusCode, String message) {
        callback(statusCode, message, null);
    }

    /**
     * The callback
     *
     * @param statusCode {@link LemonStatusCode}
     * @param body       body
     */
    default void callback(LemonStatusCode statusCode, Object body) {
        callback(statusCode, statusCode.getMessage(), body);
    }

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

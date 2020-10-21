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
        callback(statusCode, statusCode.getMessage());
    }

    /**
     * The callback
     *
     * @param statusCode {@link LemonStatusCode}
     * @param message    message
     */
    default void callback(LemonStatusCode statusCode, String message) {

    }

}

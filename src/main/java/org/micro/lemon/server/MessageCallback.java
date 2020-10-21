package org.micro.lemon.server;

import org.micro.lemon.common.LemonStatusCode;

public interface MessageCallback {

    default void callback(LemonStatusCode statusCode, String message, Object body){

    }

}

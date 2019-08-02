package org.micro.lemon.common;

import org.micro.lemon.server.LemonContext;
import org.micro.neural.extension.SPI;

import java.util.concurrent.CompletableFuture;

/**
 * Lemon Invoke
 *
 * @author lry
 */
@SPI("dubbo")
public interface LemonInvoke {

    /**
     * The initialize
     *
     * @param lemonConfig {@link LemonConfig}
     */
    void initialize(LemonConfig lemonConfig);

    /**
     * The sync invoke
     *
     * @param context {@link LemonContext}
     * @return result object
     */
    Object invoke(LemonContext context);

    /**
     * The async invoke
     *
     * @param context {@link LemonContext}
     * @return result object {@link CompletableFuture}
     */
    CompletableFuture<Object> invokeAsync(LemonContext context);

    /**
     * The build failure status code
     *
     * @param context   {@link LemonContext}
     * @param throwable throw exception
     * @return {@link LemonStatusCode}
     */
    LemonStatusCode failure(LemonContext context, Throwable throwable);

    /**
     * The destroy
     */
    void destroy();

}

package org.micro.lemon.common;

import org.micro.lemon.server.LemonContext;
import org.micro.lemon.extension.SPI;

import java.util.concurrent.CompletableFuture;

/**
 * LemonInvoke
 *
 * @author lry
 */
@SPI(value = "dubbo", single = true)
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
     * @param lemonContext {@link LemonContext}
     */
    LemonContext invoke(LemonContext lemonContext);

    /**
     * The async invoke
     *
     * @param lemonContext {@link LemonContext}
     * @return result object {@link CompletableFuture}
     */
    default CompletableFuture<LemonContext> invokeAsync(LemonContext lemonContext) {
        return CompletableFuture.completedFuture(invoke(lemonContext));
    }

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

package org.micro.lemon.common;

import org.micro.lemon.server.LemonContext;
import org.micro.lemon.extension.SPI;

import java.util.concurrent.CompletableFuture;

/**
 * Lemon Invoke
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
     * @param context {@link LemonContext}
     * @return result object
     */
    LemonContext invoke(LemonContext context);

    /**
     * The async invoke
     *
     * @param context {@link LemonContext}
     * @return result object {@link CompletableFuture}
     */
   default CompletableFuture<LemonContext> invokeAsync(LemonContext context){
       return CompletableFuture.completedFuture(invoke(context));
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

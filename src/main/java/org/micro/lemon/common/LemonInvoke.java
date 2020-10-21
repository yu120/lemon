package org.micro.lemon.common;

import org.micro.lemon.server.LemonContext;
import org.micro.lemon.extension.SPI;
import org.micro.lemon.server.LemonRequest;
import org.micro.lemon.server.LemonResponse;

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
     * @param request {@link LemonRequest}
     * @return result object
     */
    LemonResponse invoke(LemonRequest request);

    /**
     * The async invoke
     *
     * @param request {@link LemonRequest}
     * @return result object {@link CompletableFuture}
     */
   default CompletableFuture<LemonResponse> invokeAsync(LemonRequest request){
       return CompletableFuture.completedFuture(invoke(request));
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

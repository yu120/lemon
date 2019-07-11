package cn.micro.lemon;

import cn.micro.lemon.filter.LemonContext;
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
     * @param microConfig {@link MicroConfig}
     */
    void initialize(MicroConfig microConfig);

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
     * The destroy
     */
    void destroy();

}

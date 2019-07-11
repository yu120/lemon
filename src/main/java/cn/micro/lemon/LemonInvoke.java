package cn.micro.lemon;

import cn.micro.lemon.filter.LemonContext;

import java.util.concurrent.CompletableFuture;

public interface LemonInvoke {

    void initialize(MicroConfig microConfig);

    Object invoke(LemonContext context);

    CompletableFuture<Object> invokeAsync(LemonContext context);

    /**
     * The destroy
     */
    void destroy();

}

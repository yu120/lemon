package org.micro.lemon.proxy.eurake;

import lombok.extern.slf4j.Slf4j;
import org.micro.lemon.common.LemonConfig;
import org.micro.lemon.common.LemonInvoke;
import org.micro.lemon.common.LemonStatusCode;
import org.micro.lemon.extension.Extension;
import org.micro.lemon.server.LemonContext;

import java.util.concurrent.CompletableFuture;

/**
 * Eurake Lemon Invoke
 *
 * @author lry
 */
@Slf4j
@Extension("eurake")
public class EurakeInvoke implements LemonInvoke {

    @Override
    public void initialize(LemonConfig lemonConfig) {

    }

    @Override
    public Object invoke(LemonContext context) {
        return null;
    }

    @Override
    public CompletableFuture<Object> invokeAsync(LemonContext context) {
        return null;
    }

    @Override
    public LemonStatusCode failure(LemonContext context, Throwable throwable) {
        return null;
    }

    @Override
    public void destroy() {

    }

}

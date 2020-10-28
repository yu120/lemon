package org.micro.lemon.filter;

import org.micro.lemon.common.LemonConfig;
import org.micro.lemon.server.LemonContext;
import org.micro.lemon.extension.SPI;

/**
 * IFilter
 *
 * @author lry
 */
@SPI
public interface IFilter {

    /**
     * The initialize
     *
     * @param lemonConfig {@link LemonConfig}
     */
    default void initialize(LemonConfig lemonConfig) {

    }

    /**
     * The pre filter
     *
     * @param chain   {@link LemonChain}
     * @param context {@link LemonContext}
     * @throws Throwable throw exception
     */
    default void preFilter(LemonChain chain, LemonContext context) throws Throwable {

    }

    /**
     * The post filter
     *
     * @param chain   {@link LemonChain}
     * @param context {@link LemonContext}
     * @throws Throwable throw exception
     */
    default void postFilter(LemonChain chain, LemonContext context) throws Throwable {

    }

    /**
     * The destroy
     */
    default void destroy() {

    }

}

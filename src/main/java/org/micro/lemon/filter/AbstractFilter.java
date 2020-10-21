package org.micro.lemon.filter;

import org.micro.lemon.common.LemonConfig;
import org.micro.lemon.server.LemonContext;
import lombok.extern.slf4j.Slf4j;

/**
 * AbstractFilter
 *
 * @author lry
 */
@Slf4j
public abstract class AbstractFilter implements IFilter {

    @Override
    public void initialize(LemonConfig lemonConfig) {

    }

    @Override
    public void preFilter(LemonChain chain, LemonContext context) throws Throwable {
        chain.doFilter(context);
    }

    @Override
    public void postFilter(LemonChain chain, LemonContext context) throws Throwable {
        chain.doFilter(context);
    }

    @Override
    public void destroy() {

    }

}

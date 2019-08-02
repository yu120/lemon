package org.micro.lemon.filter;

import org.micro.lemon.common.LemonConfig;
import org.micro.lemon.server.LemonContext;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract Filter
 *
 * @author lry
 */
@Slf4j
public abstract class AbstractFilter implements IFilter {

    @Override
    public void initialize(LemonConfig lemonConfig) {
        log.info("The initialize filter[{}]", this);
    }

    @Override
    public void preFilter(LemonChain chain, LemonContext context) throws Throwable {
        log.info("The pre filter[{}]", this);
        chain.doFilter(context);
    }

    @Override
    public void postFilter(LemonChain chain, LemonContext context) throws Throwable {
        log.info("The post filter[{}]", this);
        chain.doFilter(context);
    }

    @Override
    public void destroy() {
        log.info("The destroy filter[{}]", this);
    }

}

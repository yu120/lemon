package org.micro.lemon.filter.support;

import org.micro.lemon.filter.IFilter;
import org.micro.lemon.filter.LemonChain;
import org.micro.lemon.server.LemonContext;
import lombok.extern.slf4j.Slf4j;
import org.micro.lemon.extension.Extension;

/**
 * LemonLogFilter
 *
 * @author lry
 */
@Slf4j
@Extension(value = "log", order = 10)
public class LemonLogFilter implements IFilter {

    @Override
    public void preFilter(LemonChain chain, LemonContext context) throws Throwable {
        log.info("The pre filter: {}", context.getRequest().getHeaders());
        chain.doFilter(context);
    }

    @Override
    public void postFilter(LemonChain chain, LemonContext context) throws Throwable {
        log.info("The post filter: {}", context.getRequest().getHeaders());
        chain.doFilter(context);
    }

}

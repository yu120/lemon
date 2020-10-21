package org.micro.lemon.filter.support;

import org.micro.lemon.filter.AbstractFilter;
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
public class LemonLogFilter extends AbstractFilter {

    @Override
    public void preFilter(LemonChain chain, LemonContext context) throws Throwable {
        log.info("The pre filter: {}", context.getRequest().getHeaders());
        super.preFilter(chain, context);
    }

    @Override
    public void postFilter(LemonChain chain, LemonContext context) throws Throwable {
        log.info("The post filter: {}", context.getRequest().getHeaders());
        super.postFilter(chain, context);
    }

}

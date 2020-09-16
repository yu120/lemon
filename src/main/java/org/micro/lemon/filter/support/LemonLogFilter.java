package org.micro.lemon.filter.support;

import org.micro.lemon.filter.AbstractFilter;
import org.micro.lemon.filter.LemonChain;
import org.micro.lemon.server.LemonContext;
import lombok.extern.slf4j.Slf4j;
import org.micro.lemon.extension.Extension;

/**
 * Lemon Log Filter
 *
 * @author lry
 */
@Slf4j
@Extension(value = "log", order = 10)
public class LemonLogFilter extends AbstractFilter {

    @Override
    public void preFilter(LemonChain chain, LemonContext context) throws Throwable {
        log.debug("The pre log lemon context: {}", context);
        super.preFilter(chain, context);
    }

    @Override
    public void postFilter(LemonChain chain, LemonContext context) throws Throwable {
        log.debug("The post log lemon context: {}", context);
        super.postFilter(chain, context);
    }

}

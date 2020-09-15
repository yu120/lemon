package org.micro.lemon.filter.support;

import org.micro.lemon.filter.AbstractFilter;
import org.micro.lemon.filter.LemonChain;
import org.micro.lemon.server.LemonContext;
import lombok.extern.slf4j.Slf4j;
import org.micro.lemon.extension.Extension;

/**
 * Lemon Cache Filter
 *
 * @author lry
 */
@Slf4j
@Extension(value = "cache", order = 150)
public class LemonCacheFilter extends AbstractFilter {

    @Override
    public void preFilter(LemonChain chain, LemonContext context) throws Throwable {
        super.preFilter(chain, context);
    }

    @Override
    public void postFilter(LemonChain chain, LemonContext context) throws Throwable {
        super.postFilter(chain, context);
    }

}

package org.micro.lemon.filter.support;

import org.micro.lemon.common.LemonStatusCode;
import org.micro.lemon.filter.AbstractFilter;
import org.micro.lemon.filter.LemonChain;
import org.micro.lemon.server.LemonContext;
import lombok.extern.slf4j.Slf4j;
import org.micro.lemon.extension.Extension;

/**
 * Lemon Exception Filter
 *
 * @author lry
 */
@Slf4j
@Extension(value = "exception", order = 0)
public class LemonExceptionFilter extends AbstractFilter {

    @Override
    public void preFilter(LemonChain chain, LemonContext context) throws Throwable {
        try {
            super.preFilter(chain, context);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            context.writeAndFlush(LemonStatusCode.INTERNAL_SERVER_ERROR);
        }
    }

}

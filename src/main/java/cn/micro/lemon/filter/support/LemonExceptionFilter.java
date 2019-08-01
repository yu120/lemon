package cn.micro.lemon.filter.support;

import cn.micro.lemon.common.LemonStatusCode;
import cn.micro.lemon.filter.AbstractFilter;
import cn.micro.lemon.filter.LemonChain;
import cn.micro.lemon.server.LemonContext;
import lombok.extern.slf4j.Slf4j;
import org.micro.neural.extension.Extension;

/**
 * Lemon Exception Filter
 *
 * @author lry
 */
@Slf4j
@Extension(value = "exception", order = 10)
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

package cn.micro.lemon.filter.support;

import cn.micro.lemon.LemonStatusCode;
import cn.micro.lemon.LemonConfig;
import cn.micro.lemon.filter.IFilter;
import cn.micro.lemon.filter.LemonChain;
import cn.micro.lemon.filter.LemonContext;
import lombok.extern.slf4j.Slf4j;
import org.micro.neural.extension.Extension;

/**
 * Lemon Exception Filter
 *
 * @author lry
 */
@Slf4j
@Extension(order = 20)
public class LemonExceptionFilter implements IFilter {

    @Override
    public void initialize(LemonConfig lemonConfig) {

    }

    @Override
    public void doFilter(LemonChain chain, LemonContext context) throws Throwable {
        try {
            chain.doFilter(context);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            context.writeAndFlush(LemonStatusCode.MICRO_ERROR_EXCEPTION, null);
        }
    }

    @Override
    public void destroy() {

    }

}

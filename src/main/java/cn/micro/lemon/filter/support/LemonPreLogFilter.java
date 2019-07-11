package cn.micro.lemon.filter.support;

import cn.micro.lemon.LemonConfig;
import cn.micro.lemon.filter.IFilter;
import cn.micro.lemon.filter.LemonChain;
import cn.micro.lemon.filter.LemonContext;
import lombok.extern.slf4j.Slf4j;
import org.micro.neural.extension.Extension;

/**
 * Lemon Log Filter
 *
 * @author lry
 */
@Slf4j
@Extension(order = 30)
public class LemonPreLogFilter implements IFilter {

    @Override
    public void initialize(LemonConfig lemonConfig) {

    }

    @Override
    public void doFilter(LemonChain chain, LemonContext context) throws Throwable {
        log.debug("The pre log lemon context: {}", context);
        chain.doFilter(context);
    }

    @Override
    public void destroy() {

    }

}

package cn.micro.lemon.filter.support;

import cn.micro.lemon.MicroConfig;
import cn.micro.lemon.filter.IFilter;
import cn.micro.lemon.filter.LemonChain;
import cn.micro.lemon.filter.LemonContext;
import lombok.extern.slf4j.Slf4j;
import org.micro.neural.extension.Extension;

@Slf4j
@Extension(order = 10)
public class LemonLogFilter implements IFilter {

    @Override
    public void initialize(MicroConfig microConfig) {

    }

    @Override
    public void doFilter(LemonChain chain, LemonContext context) throws Throwable {
        log.debug("The log lemon context: {}", context);
        chain.doFilter(context);
    }

    @Override
    public void destroy() {

    }

}

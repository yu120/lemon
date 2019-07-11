package cn.micro.lemon.filter.support;

import cn.micro.lemon.MicroConfig;
import cn.micro.lemon.filter.IFilter;
import cn.micro.lemon.filter.LemonChain;
import cn.micro.lemon.filter.LemonContext;
import lombok.extern.slf4j.Slf4j;
import org.micro.neural.extension.Extension;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * Lemon Trace Filter
 *
 * @author lry
 */
@Slf4j
@Extension(order = 10)
public class LemonTraceFilter implements IFilter {

    @Override
    public void initialize(MicroConfig microConfig) {

    }

    @Override
    public void doFilter(LemonChain chain, LemonContext context) throws Throwable {
        MDC.put(LemonContext.TRACE_KEY, UUID.randomUUID().toString());
        chain.doFilter(context);
    }

    @Override
    public void destroy() {

    }

}

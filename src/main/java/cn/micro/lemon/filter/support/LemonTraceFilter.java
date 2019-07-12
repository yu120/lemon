package cn.micro.lemon.filter.support;

import cn.micro.lemon.filter.AbstractFilter;
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
public class LemonTraceFilter extends AbstractFilter {

    @Override
    public void preFilter(LemonChain chain, LemonContext context) throws Throwable {
        MDC.put(LemonContext.LEMON_ID, UUID.randomUUID().toString());
        super.preFilter(chain, context);
    }

}

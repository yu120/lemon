package cn.micro.lemon.filter.support;

import cn.micro.lemon.filter.AbstractFilter;
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

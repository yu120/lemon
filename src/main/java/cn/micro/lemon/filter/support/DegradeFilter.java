package cn.micro.lemon.filter.support;

import cn.micro.lemon.filter.AbstractFilter;
import cn.micro.lemon.filter.LemonChain;
import cn.micro.lemon.server.LemonContext;
import lombok.extern.slf4j.Slf4j;
import org.micro.neural.extension.Extension;

/**
 * Degrade Filter
 * <p>
 * TODO
 *
 * @author lry
 */
@Slf4j
@Extension(order = 170)
public class DegradeFilter extends AbstractFilter {

    @Override
    public void preFilter(LemonChain chain, LemonContext context) throws Throwable {
        super.preFilter(chain, context);
    }

    @Override
    public void postFilter(LemonChain chain, LemonContext context) throws Throwable {
        super.postFilter(chain, context);
    }

}

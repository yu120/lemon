package cn.micro.lemon.filter;

import cn.micro.lemon.LemonConfig;

/**
 * Abstract Filter
 *
 * @author lry
 */
public abstract class AbstractFilter implements IFilter {

    @Override
    public void initialize(LemonConfig lemonConfig) {

    }

    @Override
    public void preFilter(LemonChain chain, LemonContext context) throws Throwable {
        chain.doFilter(context);
    }

    @Override
    public void postFilter(LemonChain chain, LemonContext context) throws Throwable {
        chain.doFilter(context);
    }

    @Override
    public void destroy() {

    }

}

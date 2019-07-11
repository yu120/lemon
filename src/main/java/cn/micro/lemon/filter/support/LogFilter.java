package cn.micro.lemon.filter.support;

import cn.micro.lemon.MicroConfig;
import cn.micro.lemon.filter.IFilter;
import cn.micro.lemon.filter.LemonChain;
import cn.micro.lemon.filter.LemonContext;
import org.micro.neural.extension.Extension;

@Extension(order = 10)
public class LogFilter implements IFilter {

    @Override
    public void initialize(MicroConfig microConfig) {

    }

    @Override
    public void doFilter(LemonChain chain, LemonContext context) throws Throwable {
        chain.doFilter(context);
    }

    @Override
    public void destroy() {

    }

}

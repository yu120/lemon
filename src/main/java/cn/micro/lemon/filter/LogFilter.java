package cn.micro.lemon.filter;

import cn.micro.lemon.MicroConfig;
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

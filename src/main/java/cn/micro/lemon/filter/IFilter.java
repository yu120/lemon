package cn.micro.lemon.filter;

import cn.micro.lemon.LemonConfig;
import org.micro.neural.extension.SPI;

/**
 * Filter
 *
 * @author lry
 */
@SPI
public interface IFilter {

    /**
     * The initialize
     *
     * @param lemonConfig {@link LemonConfig}
     */
    void initialize(LemonConfig lemonConfig);

    /**
     * The filter
     *
     * @param chain   {@link LemonChain}
     * @param context {@link LemonContext}
     * @throws Throwable throw exception
     */
    void doFilter(LemonChain chain, LemonContext context) throws Throwable;

    /**
     * The destroy
     */
    void destroy();

}

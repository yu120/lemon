package cn.micro.lemon.filter;

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
     * @throws Exception throw exception
     */
    void initialize() throws Exception;

    /**
     * The filter
     *
     * @param context {@link LemonContext}
     * @throws Throwable throw exception
     */
    void doFilter(LemonChain chain, LemonContext context) throws Throwable;

    /**
     * The destroy
     *
     * @throws Exception throw exception
     */
    void destroy() throws Exception;

}

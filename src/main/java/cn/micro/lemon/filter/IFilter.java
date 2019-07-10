package cn.micro.lemon.filter;

/**
 * Filter
 *
 * @author lry
 */
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
     * @param chainContext {@link ChainContext}
     * @throws Throwable throw exception
     */
    void doFilter(ChainContext chainContext) throws Throwable;

    /**
     * The destroy
     *
     * @throws Exception throw exception
     */
    void destroy() throws Exception;

}

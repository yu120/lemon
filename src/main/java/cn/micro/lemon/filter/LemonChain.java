package cn.micro.lemon.filter;

import cn.micro.lemon.server.LemonContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lemon Chain
 *
 * @author lry
 */
@Slf4j
public class LemonChain {

    private AtomicBoolean flag = new AtomicBoolean(true);
    private AtomicInteger index = new AtomicInteger(0);

    /**
     * The do filter
     *
     * @param context {@link LemonContext}
     * @throws Throwable throw exception
     */
    public void doFilter(LemonContext context) throws Throwable {
        LemonChainFactory factory = LemonChainFactory.INSTANCE;
        List<IFilter> filters = factory.getFilters();

        if (flag.get()) {
            int tempIndex = index.getAndIncrement();
            if (tempIndex >= factory.getRouterFilterIndex()) {
                flag.set(false);
            }

            IFilter filter = filters.get(tempIndex);
            if (filter != null) {
                filter.preFilter(this, context);
            }
        } else {
            if (index.get() <= 0) {
                return;
            }

            int tempIndex = index.decrementAndGet();
            IFilter filter = filters.get(tempIndex);
            if (filter != null) {
                filter.postFilter(this, context);
            }
        }
    }

}

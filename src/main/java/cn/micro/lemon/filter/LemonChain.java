package cn.micro.lemon.filter;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
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

    public final static String ROUTER = "ROUTER";

    private int filterSize;
    private List<IFilter> filters = new ArrayList<>();
    private AtomicBoolean flag = new AtomicBoolean(true);
    private AtomicInteger index = new AtomicInteger(0);

    private LemonChain() {
        this.filters.addAll(FilterFactory.INSTANCE.getFilters());
        this.filterSize = filters.size();
    }

    /**
     * The processor
     *
     * @param context {@link LemonContext}
     * @throws Throwable throw exception
     */
    public static void processor(LemonContext context) throws Throwable {
        new LemonChain().doFilter(context);
    }

    /**
     * The do filter
     *
     * @param context {@link LemonContext}
     * @throws Throwable throw exception
     */
    public void doFilter(LemonContext context) throws Throwable {
        if (flag.get()) {
            int tempIndex = index.getAndIncrement();
            IFilter filter = filters.get(tempIndex);
            if (filter != null) {
                log.info("The pre filter[{}]", filter);
                filter.preFilter(this, context);
            }

            if (tempIndex + 1 >= filterSize) {
                flag.set(false);
            }
        } else {
            if (index.get() <= 0) {
                return;
            }

            int tempIndex = index.decrementAndGet();
            IFilter filter = filters.get(tempIndex);
            if (filter != null) {
                log.info("The post filter[{}]", filter);
                filter.postFilter(this, context);
            }
        }
    }

}

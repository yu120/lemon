package org.micro.lemon.filter;

import org.micro.lemon.server.LemonContext;
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

    private AtomicBoolean flag = new AtomicBoolean(true);
    private AtomicInteger index = new AtomicInteger(0);

    private int filterMaxIndex = 0;
    private List<IFilter> filters = new ArrayList<>();

    /**
     * The do filter
     *
     * @param context {@link LemonContext}
     * @throws Throwable throw exception
     */
    public final void start(LemonContext context, List<IFilter> filters) throws Throwable {
        if (!filters.isEmpty()) {
            this.filterMaxIndex = filters.size() - 1;
            this.filters.addAll(filters);
        }
        doFilter(context);
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
            if (tempIndex >= filterMaxIndex) {
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

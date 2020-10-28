package org.micro.lemon.filter;

import org.micro.lemon.common.LemonStatusCode;
import org.micro.lemon.server.LemonContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * LemonChain
 *
 * @author lry
 */
@Slf4j
public class LemonChain {

    private AtomicInteger index = new AtomicInteger(0);
    private AtomicBoolean flag = new AtomicBoolean(true);

    private LemonContext lemonContext;
    private int filterMaxIndex = 0;
    private List<IFilter> filters = new ArrayList<>();

    /**
     * The do filter
     *
     * @param lemonContext {@link LemonContext}
     */
    public LemonChain(LemonContext lemonContext) {
        List<IFilter> filters = LemonFactory.INSTANCE.getFilters();
        if (!filters.isEmpty()) {
            this.filterMaxIndex = filters.size() - 1;
            this.filters.addAll(filters);
        }
        this.lemonContext = lemonContext;
    }

    /**
     * The start filter chain
     */
    public void start0() {
        MDC.put(LemonContext.LEMON_ID_KEY, lemonContext.getRequest().getRequestId());

        try {
            doFilter(lemonContext);
        } catch (Throwable t) {
            log.error(LemonStatusCode.INTERNAL_SERVER_ERROR.getMessage(), t);
            lemonContext.callback(LemonStatusCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * The do filter
     *
     * @param context {@link LemonContext}
     * @throws Throwable throw exception
     */
    protected void doFilter(LemonContext context) throws Throwable {
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

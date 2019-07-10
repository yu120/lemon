package cn.micro.lemon.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LemonChain {

    private List<IFilter> filters = new ArrayList<>();
    private AtomicInteger index = new AtomicInteger(0);

    public LemonChain() {
        this.filters.addAll(FilterFactory.INSTANCE.getFilters());
    }

    public void doFilter(LemonContext context) {
        if (index.get() == filters.size()) {
            return;
        }

        IFilter filter = filters.get(index.getAndIncrement());
        if (filter == null) {
            return;
        }

        try {
            filter.doFilter(this, context);
        } catch (Throwable t) {

        }
    }

}

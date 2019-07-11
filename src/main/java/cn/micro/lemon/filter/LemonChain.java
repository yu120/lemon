package cn.micro.lemon.filter;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LemonChain {

    private List<IFilter> filters = new ArrayList<>();
    private AtomicInteger index = new AtomicInteger(0);

    private LemonChain() {
        this.filters.addAll(FilterFactory.INSTANCE.getFilters());
    }

    public void doFilter(LemonContext context) throws Throwable {
        if (index.get() == filters.size()) {
            return;
        }

        IFilter filter = filters.get(index.getAndIncrement());
        if (filter == null) {
            return;
        }

        filter.doFilter(this, context);
    }

    public static void processor(LemonContext context) throws Throwable {
        new LemonChain().doFilter(context);
    }

}

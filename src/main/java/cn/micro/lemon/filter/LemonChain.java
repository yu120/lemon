package cn.micro.lemon.filter;

import cn.micro.lemon.LemonConfig;
import lombok.extern.slf4j.Slf4j;
import org.micro.neural.extension.Extension;
import org.micro.neural.extension.ExtensionLoader;

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

    private static int filterSize;
    private static List<IFilter> filters = new ArrayList<>();

    private AtomicBoolean flag = new AtomicBoolean(true);
    private AtomicInteger index = new AtomicInteger(0);

    public static void initialize(LemonConfig lemonConfig) {
        List<IFilter> filterList = ExtensionLoader.getLoader(IFilter.class).getExtensions();
        if (filterList.size() > 0) {
            for (IFilter filter : filterList) {
                Extension extension = filter.getClass().getAnnotation(Extension.class);
                if (extension != null) {
                    filters.add(filter);
                }
            }
        }

        for (IFilter filter : filters) {
            filter.initialize(lemonConfig);
            log.info("The filter[{}] initialize is success.", filter);
        }
        filterSize = filters.size();
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
                filter.postFilter(this, context);
            }
        }
    }

}

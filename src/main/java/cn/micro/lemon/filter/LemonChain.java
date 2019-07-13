package cn.micro.lemon.filter;

import cn.micro.lemon.LemonConfig;
import lombok.extern.slf4j.Slf4j;
import org.micro.neural.extension.Extension;
import org.micro.neural.extension.ExtensionLoader;

import java.util.ArrayList;
import java.util.Arrays;
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

    private static int routerFilterIndex = -1;
    private static List<IFilter> filters = new ArrayList<>();

    private AtomicBoolean flag = new AtomicBoolean(true);
    private AtomicInteger index = new AtomicInteger(0);

    private LemonChain() {

    }

    /**
     * The initialize filter chain
     *
     * @param lemonConfig {@link LemonConfig}
     */
    public static void initialize(LemonConfig lemonConfig) {
        List<IFilter> filterList = ExtensionLoader.getLoader(IFilter.class).getExtensions();
        if (filterList.size() > 0) {
            for (int i = 0; i < filterList.size(); i++) {
                IFilter filter = filterList.get(i);
                Extension extension = filter.getClass().getAnnotation(Extension.class);
                if (extension != null) {
                    if (routerFilterIndex < 0 && Arrays.asList(extension.category()).contains(ROUTER)) {
                        routerFilterIndex = i;
                    }
                    filters.add(filter);
                }
            }
        }

        for (IFilter filter : filters) {
            filter.initialize(lemonConfig);
            log.info("The filter[{}] initialize is success.", filter);
        }
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
            if (tempIndex >= routerFilterIndex) {
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

    /**
     * The destroy filter chain
     */
    public static void destroy() {
        for (IFilter filter : filters) {
            filter.destroy();
            log.info("The filter[{}] destroy.", filter);
        }
    }

}

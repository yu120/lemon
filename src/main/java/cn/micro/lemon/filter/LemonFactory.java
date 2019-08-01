package cn.micro.lemon.filter;

import cn.micro.lemon.common.LemonConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.micro.neural.extension.Extension;
import org.micro.neural.extension.ExtensionLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Lemon Chain Factory
 *
 * @author lry
 */
@Slf4j
@Getter
public enum LemonFactory {

    // ====

    INSTANCE;

    public final static String ROUTER = "ROUTER";

    private int routerFilterIndex = -1;
    private List<IFilter> filters = new ArrayList<>();

    /**
     * The initialize filter chain
     *
     * @param lemonConfig {@link LemonConfig}
     */
    public void initialize(LemonConfig lemonConfig) {
        List<IFilter> filterList = ExtensionLoader.getLoader(IFilter.class).getExtensions();
        if (filterList.size() > 0) {
            for (int i = 0; i < filterList.size(); i++) {
                IFilter filter = filterList.get(i);
                Extension extension = filter.getClass().getAnnotation(Extension.class);
                if (extension != null) {
                    // calculation filter id
                    String id = extension.value();
                    if (id.trim().length() == 0) {
                        id = filter.getClass().getSimpleName();
                    }

                    // exclude filter by id
                    if (!lemonConfig.getExcludeFilters().isEmpty()) {
                        if (lemonConfig.getExcludeFilters().contains(id)) {
                            continue;
                        }
                    }

                    // include filter by id
                    if (!lemonConfig.getIncludeFilters().isEmpty()) {
                        if (!lemonConfig.getIncludeFilters().contains(id)) {
                            continue;
                        }
                    }

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
     * The destroy filter chain
     */
    public void destroy() {
        for (IFilter filter : filters) {
            filter.destroy();
            log.info("The filter[{}] destroy.", filter);
        }
    }

}

package org.micro.lemon.filter;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.micro.lemon.common.LemonConfig;
import org.micro.lemon.extension.Extension;
import org.micro.lemon.extension.ExtensionLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LemonFactory
 *
 * @author lry
 */
@Slf4j
@Getter
public enum LemonFactory {

    // ====

    INSTANCE;

    public final static String ROUTER = "ROUTER";

    private List<IFilter> filters = new ArrayList<>();
    private Map<Class<?>, IFilter> filterMap = new ConcurrentHashMap<>();

    /**
     * The initialize filter chain
     *
     * @param lemonConfig {@link LemonConfig}
     */
    public void initialize(LemonConfig lemonConfig) {
        List<IFilter> filterList = ExtensionLoader.getLoader(IFilter.class).getExtensions();
        if (filterList.size() > 0) {
            for (IFilter filter : filterList) {
                Extension extension = filter.getClass().getAnnotation(Extension.class);
                if (extension == null) {
                    continue;
                }

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
                filters.add(filter);
                filterMap.put(filter.getClass(), filter);
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

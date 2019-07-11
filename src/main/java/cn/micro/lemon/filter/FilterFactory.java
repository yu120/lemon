package cn.micro.lemon.filter;

import cn.micro.lemon.LemonConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.micro.neural.extension.Extension;
import org.micro.neural.extension.ExtensionLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Filter Factory
 *
 * @author lry
 */
@Slf4j
@Getter
public enum FilterFactory {

    // ====

    INSTANCE;

    private final List<IFilter> filters = new ArrayList<>();

    public void initialize(LemonConfig lemonConfig) {
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
    }

    public void destroy() {
        for (IFilter filter : filters) {
            filter.destroy();
        }
    }

}

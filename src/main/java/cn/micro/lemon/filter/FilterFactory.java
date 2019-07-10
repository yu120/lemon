package cn.micro.lemon.filter;

import lombok.extern.slf4j.Slf4j;
import org.micro.neural.extension.Extension;
import org.micro.neural.extension.ExtensionLoader;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

@Slf4j
public class FilterFactory {

    private final ConcurrentMap<String, IFilter> filters = new ConcurrentSkipListMap<>();

    public FilterFactory() {
        try {
            List<IFilter> filterList = ExtensionLoader.getLoader(IFilter.class).getExtensions();
            if (filterList.size() > 0) {
                for (IFilter filter : filterList) {
                    log.debug("The add filter: {}", filter.getClass().getName());

                    Extension extension = filter.getClass().getAnnotation(Extension.class);
                    if (extension != null) {
                        filters.put(filter.getClass().getName(), filter);
                    }
                }
            }
        } catch (Exception e) {
            log.error("The start filter chain is exception", e);
        }
    }

}

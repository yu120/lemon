package cn.micro.lemon.filter;

import cn.micro.lemon.MicroConfig;
import lombok.Getter;
import org.micro.neural.extension.Extension;
import org.micro.neural.extension.ExtensionLoader;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum FilterFactory {

    // ====

    INSTANCE;

    private final List<IFilter> filters = new ArrayList<>();

    public void initialize(MicroConfig microConfig) {
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
            filter.initialize(microConfig);
        }
    }

    public void destroy() {
        for (IFilter filter : filters) {
            filter.destroy();
        }
    }

}

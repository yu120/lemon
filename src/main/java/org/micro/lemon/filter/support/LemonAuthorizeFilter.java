package org.micro.lemon.filter.support;

import lombok.extern.slf4j.Slf4j;
import org.micro.lemon.common.LemonConfig;
import org.micro.lemon.common.ServiceMapping;
import org.micro.lemon.extension.Extension;
import org.micro.lemon.filter.AbstractFilter;
import org.micro.lemon.filter.LemonChain;
import org.micro.lemon.proxy.dubbo.RegistryServiceSubscribe;
import org.micro.lemon.server.LemonContext;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Lemon Authorize Filter
 *
 * @author lry
 */
@Slf4j
@Extension(value = "authorize", order = 30)
public class LemonAuthorizeFilter extends AbstractFilter {

    private static final String GROUP_KEY = "group";
    private static final String VERSION_KEY = "version";

    private RegistryServiceSubscribe registryServiceSubscribe;

    @Override
    public void initialize(LemonConfig lemonConfig) {
        this.registryServiceSubscribe = new RegistryServiceSubscribe();
        registryServiceSubscribe.initialize();
    }

    @Override
    public void preFilter(LemonChain chain, LemonContext context) throws Throwable {
        wrapperServiceDefinition(context);
        super.preFilter(chain, context);
    }

    @Override
    public void postFilter(LemonChain chain, LemonContext context) throws Throwable {
        super.postFilter(chain, context);
    }

    @Override
    public void destroy() {
        if (registryServiceSubscribe != null) {
            registryServiceSubscribe.destroy();
        }
    }

    /**
     * The wrapper {@link ServiceMapping} by {@link LemonContext}
     *
     * @param context {@link LemonContext}
     */
    private void wrapperServiceDefinition(LemonContext context) {
        List<String> paths = context.getPaths();
        if (paths.size() != 4) {
            throw new IllegalArgumentException("Illegal Request");
        }

        ServiceMapping serviceMapping = new ServiceMapping();
        serviceMapping.setApplication(paths.get(1));
        serviceMapping.setService(paths.get(2));
        serviceMapping.setMethod(paths.get(3));

        // wrapper service name
        wrapperServiceName(serviceMapping);

        Map<String, Object> parameters = context.getHeaders();
        if (parameters.containsKey(GROUP_KEY)) {
            serviceMapping.setGroup(String.valueOf(parameters.get(GROUP_KEY)));
        }
        if (parameters.containsKey(VERSION_KEY)) {
            serviceMapping.setVersion(String.valueOf(parameters.get(VERSION_KEY)));
        }

        context.setServiceMapping(serviceMapping);
    }

    private void wrapperServiceName(ServiceMapping serviceMapping) {
        ConcurrentMap<String, String> serviceNames =
                registryServiceSubscribe.getServiceNames().get(serviceMapping.getApplication());
        if (serviceNames == null || serviceNames.isEmpty()) {
            serviceMapping.setServiceName(serviceMapping.getService());
            return;
        }

        String serviceName = serviceNames.get(serviceMapping.getService());
        if (serviceName == null || serviceName.length() == 0) {
            serviceMapping.setServiceName(serviceMapping.getService());
            return;
        }

        serviceMapping.setServiceName(serviceName);
    }

}

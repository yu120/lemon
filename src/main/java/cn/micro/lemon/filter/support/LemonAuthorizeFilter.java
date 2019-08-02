package cn.micro.lemon.filter.support;

import cn.micro.lemon.common.LemonConfig;
import cn.micro.lemon.common.ServiceMappingWrapper;
import cn.micro.lemon.filter.AbstractFilter;
import cn.micro.lemon.filter.LemonChain;
import cn.micro.lemon.proxy.dubbo.RegistryServiceSubscribe;
import cn.micro.lemon.server.LemonContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.utils.StringUtils;
import org.micro.neural.common.URL;
import org.micro.neural.extension.Extension;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Lemon Authorize Filter
 *
 * @author lry
 */
@Slf4j
@Extension(value = "authorize", order = 30)
public class LemonAuthorizeFilter extends AbstractFilter {

    private RegistryServiceSubscribe registryServiceSubscribe;
    private Map<String, URL> services = new ConcurrentHashMap<>();

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
     * The build {@link ServiceMappingWrapper} by {@link LemonContext}
     *
     * @param context {@link LemonContext}
     */
    private void wrapperServiceDefinition(LemonContext context) {
        List<String> paths = context.getPaths();
        if (paths.size() != 4) {
            throw new IllegalArgumentException("Illegal Request");
        }

        ServiceMappingWrapper serviceMappingWrapper = new ServiceMappingWrapper();
        serviceMappingWrapper.setApplication(paths.get(1));
        serviceMappingWrapper.setService(paths.get(2));
        serviceMappingWrapper.setMethod(paths.get(3));

        // wrapper service name
        wrapperServiceName(serviceMappingWrapper);

        Map<String, String> parameters = context.getParameters();
        if (parameters.containsKey(CommonConstants.GROUP_KEY)) {
            String group = parameters.get(CommonConstants.GROUP_KEY);
            if (group != null && group.length() > 0) {
                serviceMappingWrapper.setGroup(group);
            }
        }
        if (parameters.containsKey(CommonConstants.VERSION_KEY)) {
            String version = parameters.get(CommonConstants.VERSION_KEY);
            if (version != null && version.length() > 0) {
                serviceMappingWrapper.setVersion(version);
            }
        }

        context.setServiceMappingWrapper(serviceMappingWrapper);
    }

    private void wrapperServiceName(ServiceMappingWrapper serviceMappingWrapper) {
        ConcurrentMap<String, String> serviceNames =
                registryServiceSubscribe.getServiceNames().get(serviceMappingWrapper.getApplication());
        if (serviceNames == null || serviceNames.isEmpty()) {
            serviceMappingWrapper.setServiceName(serviceMappingWrapper.getService());
            return;
        }

        String serviceName = serviceNames.get(serviceMappingWrapper.getService());
        if (StringUtils.isBlank(serviceName)) {
            serviceMappingWrapper.setServiceName(serviceMappingWrapper.getService());
            return;
        }

        serviceMappingWrapper.setServiceName(serviceName);
    }

}

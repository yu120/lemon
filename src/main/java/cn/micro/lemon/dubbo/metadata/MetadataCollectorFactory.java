package cn.micro.lemon.dubbo.metadata;

import cn.micro.lemon.common.LemonConfig;
import cn.micro.lemon.dubbo.MetadataCollector;
import cn.micro.lemon.common.ServiceDefinition;
import cn.micro.lemon.server.LemonContext;
import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.metadata.definition.model.FullServiceDefinition;
import org.apache.dubbo.metadata.definition.model.MethodDefinition;
import org.apache.dubbo.metadata.identifier.MetadataIdentifier;
import org.micro.neural.common.URL;
import org.micro.neural.extension.ExtensionLoader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Metadata Collector Factory
 *
 * @author lry
 */
public enum MetadataCollectorFactory {

    // ====

    INSTANCE;

    private final static String MAXIMUM_KEY = "cacheMaximum";
    private final static String DURATION_KEY = "cacheDuration";
    private LemonConfig lemonConfig;
    private MetadataCollector metadataCollector;
    private Cache<String, FullServiceDefinition> cache = null;

    /**
     * The initialize
     *
     * @param lemonConfig {@link LemonConfig}
     */
    public void initialize(LemonConfig lemonConfig) {
        this.lemonConfig = lemonConfig;
        if (StringUtils.isNotEmpty(lemonConfig.getDubbo().getMetadataAddress())) {
            URL metadataUrl = URL.valueOf(lemonConfig.getDubbo().getMetadataAddress());

            CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
            builder.maximumSize(metadataUrl.getParameter(MAXIMUM_KEY, 2000));
            builder.expireAfterAccess(metadataUrl.getParameter(
                    DURATION_KEY, 2 * 60 * 60L), TimeUnit.MILLISECONDS);

            this.cache = builder.build();
            this.metadataCollector = ExtensionLoader.getLoader(
                    MetadataCollector.class).getExtension(metadataUrl.getProtocol());
            metadataCollector.initialize(metadataUrl);
        }
    }

    /**
     * The invalidate cache
     *
     * @param serviceDefinitions service definition list
     */
    public void invalidates(List<ServiceDefinition> serviceDefinitions) {
        if (serviceDefinitions == null || serviceDefinitions.isEmpty()) {
            cache.invalidateAll();
        } else if (serviceDefinitions.size() == 1) {
            ServiceDefinition serviceDefinition = serviceDefinitions.get(0);
            MetadataIdentifier identifier = new MetadataIdentifier(
                    serviceDefinition.getService(), serviceDefinition.getVersion(),
                    serviceDefinition.getGroup(), CommonConstants.PROVIDER_SIDE, serviceDefinition.getApplication());
            cache.invalidate(identifier.getIdentifierKey());
        } else {
            Set<String> keys = new HashSet<>();
            for (ServiceDefinition serviceDefinition : serviceDefinitions) {
                MetadataIdentifier identifier = build(serviceDefinition);
                keys.add(identifier.getIdentifierKey());
            }

            cache.invalidateAll(keys);
        }
    }

    /**
     * The wrapper types from metadata
     *
     * @param context           {@link LemonContext}
     * @param serviceDefinition {@link ServiceDefinition}
     */
    public void wrapperTypesFromMetadata(LemonContext context, ServiceDefinition serviceDefinition) {
        MetadataIdentifier identifier = build(serviceDefinition);

        // whether to clear cached access
        String invalidateCache = context.getHeaders().get(LemonContext.INVALIDATE_CACHE);
        if (!StringUtils.isBlank(invalidateCache)) {
            if (Boolean.valueOf(invalidateCache)) {
                String lemonToken = context.getHeaders().get(LemonContext.LEMON_TOKEN);
                if (lemonConfig.getToken().equals(lemonToken)) {
                    cache.invalidate(identifier.getIdentifierKey());
                }
            }
        }

        FullServiceDefinition fullServiceDefinition;
        try {
            fullServiceDefinition = cache.get(identifier.getIdentifierKey(), () -> {
                String metadata = metadataCollector.pullMetaData(identifier);
                if (!StringUtils.isBlank(metadata)) {
                    return JSON.parseObject(metadata, FullServiceDefinition.class);
                }

                return null;
            });
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        List<MethodDefinition> methods = fullServiceDefinition.getMethods();
        if (methods == null) {
            return;
        }
        for (MethodDefinition m : methods) {
            if (!m.getName().equals(serviceDefinition.getMethod()) ||
                    m.getParameterTypes().length != serviceDefinition.getParamValues().length) {
                continue;
            }

            List<String> parameterTypes = new ArrayList<>();
            for (String parameterType : m.getParameterTypes()) {
                if (parameterType.contains("<")) {
                    parameterTypes.add(parameterType.substring(0, parameterType.indexOf("<")));
                } else {
                    parameterTypes.add(parameterType);
                }
            }

            serviceDefinition.setParamTypes(parameterTypes.toArray(new String[0]));
        }
    }

    /**
     * The build {@link ServiceDefinition}
     *
     * @param serviceDefinition {@link ServiceDefinition}
     * @return {@link MetadataIdentifier}
     */
    private MetadataIdentifier build(ServiceDefinition serviceDefinition) {
        return new MetadataIdentifier(
                serviceDefinition.getService(),
                serviceDefinition.getVersion(),
                serviceDefinition.getGroup(),
                CommonConstants.PROVIDER_SIDE,
                serviceDefinition.getApplication());
    }

}

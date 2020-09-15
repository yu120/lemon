package org.micro.lemon.proxy.dubbo.metadata;

import org.micro.lemon.common.LemonConfig;
import org.micro.lemon.common.ServiceMapping;
import org.micro.lemon.proxy.dubbo.MetadataCollector;
import org.micro.lemon.server.LemonContext;
import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.metadata.definition.model.FullServiceDefinition;
import org.apache.dubbo.metadata.definition.model.MethodDefinition;
import org.apache.dubbo.metadata.identifier.MetadataIdentifier;
import org.micro.neural.common.URL;
import org.micro.lemon.extension.ExtensionLoader;

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
     * @param serviceMappings service definition list
     */
    public void invalidates(List<ServiceMapping> serviceMappings) {
        if (serviceMappings == null || serviceMappings.isEmpty()) {
            cache.invalidateAll();
        } else if (serviceMappings.size() == 1) {
            ServiceMapping serviceMapping = serviceMappings.get(0);
            MetadataIdentifier identifier = new MetadataIdentifier(
                    serviceMapping.getService(), serviceMapping.getVersion(),
                    serviceMapping.getGroup(), CommonConstants.PROVIDER_SIDE, serviceMapping.getApplication());
            cache.invalidate(identifier.getIdentifierKey());
        } else {
            Set<String> keys = new HashSet<>();
            for (ServiceMapping serviceMapping : serviceMappings) {
                MetadataIdentifier identifier = build(serviceMapping);
                keys.add(identifier.getIdentifierKey());
            }

            cache.invalidateAll(keys);
        }
    }

    /**
     * The wrapper types from metadata
     *
     * @param context           {@link LemonContext}
     * @param serviceMapping {@link ServiceMapping}
     */
    public void wrapperTypesFromMetadata(LemonContext context, ServiceMapping serviceMapping) {
        MetadataIdentifier identifier = build(serviceMapping);

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
            if (!m.getName().equals(serviceMapping.getMethod()) ||
                    m.getParameterTypes().length != serviceMapping.getParamValues().length) {
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

            serviceMapping.setParamTypes(parameterTypes.toArray(new String[0]));
        }
    }

    /**
     * The build {@link ServiceMapping}
     *
     * @param serviceMapping {@link ServiceMapping}
     * @return {@link MetadataIdentifier}
     */
    private MetadataIdentifier build(ServiceMapping serviceMapping) {
        return new MetadataIdentifier(
                serviceMapping.getServiceName(),
                serviceMapping.getVersion(),
                serviceMapping.getGroup(),
                CommonConstants.PROVIDER_SIDE,
                serviceMapping.getApplication());
    }

}

package cn.micro.lemon.dubbo.metadata;

import cn.micro.lemon.dubbo.MetadataCollector;
import cn.micro.lemon.common.ServiceDefinition;
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
    private MetadataCollector metadataCollector;
    private Cache<String, FullServiceDefinition> cache = null;

    /**
     * The initialize
     *
     * @param metadataAddress metadata address
     */
    public void initialize(String metadataAddress) {
        if (StringUtils.isNotEmpty(metadataAddress)) {
            URL metadataUrl = URL.valueOf(metadataAddress);

            CacheBuilder<String, FullServiceDefinition> builder = new CacheBuilder<>();
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
                MetadataIdentifier identifier = new MetadataIdentifier(
                        serviceDefinition.getService(), serviceDefinition.getVersion(),
                        serviceDefinition.getGroup(), CommonConstants.PROVIDER_SIDE, serviceDefinition.getApplication());
                keys.add(identifier.getIdentifierKey());
            }

            cache.invalidateAll(keys);
        }
    }

    /**
     * The wrapper types from metadata
     *
     * @param serviceDefinition {@link ServiceDefinition}
     */
    public void wrapperTypesFromMetadata(ServiceDefinition serviceDefinition) {
        MetadataIdentifier identifier = new MetadataIdentifier(
                serviceDefinition.getService(), serviceDefinition.getVersion(),
                serviceDefinition.getGroup(), CommonConstants.PROVIDER_SIDE, serviceDefinition.getApplication());

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
            throw new RuntimeException("");
        }

        List<MethodDefinition> methods = fullServiceDefinition.getMethods();
        if (methods != null) {
            for (MethodDefinition m : methods) {
                if (sameMethod(m, serviceDefinition.getMethod(), serviceDefinition.getParamValues().length)) {
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
        }
    }

    private boolean sameMethod(MethodDefinition m, String methodName, int paramLen) {
        return (m.getName().equals(methodName) && m.getParameterTypes().length == paramLen);
    }

}

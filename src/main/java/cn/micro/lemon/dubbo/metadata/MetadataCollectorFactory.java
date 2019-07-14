package cn.micro.lemon.dubbo.metadata;

import cn.micro.lemon.dubbo.MetadataCollector;
import cn.micro.lemon.common.ServiceDefinition;
import com.alibaba.fastjson.JSON;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.metadata.definition.model.FullServiceDefinition;
import org.apache.dubbo.metadata.definition.model.MethodDefinition;
import org.apache.dubbo.metadata.identifier.MetadataIdentifier;
import org.micro.neural.common.URL;
import org.micro.neural.extension.ExtensionLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Metadata Collector Factory
 *
 * @author lry
 */
public class MetadataCollectorFactory {

    private MetadataCollector metadataCollector;
    private ConcurrentMap<String, FullServiceDefinition> serviceDefinitions = new ConcurrentHashMap<>();

    /**
     * The initialize
     *
     * @param metadataAddress metadata address
     */
    public void initialize(String metadataAddress) {
        if (StringUtils.isNotEmpty(metadataAddress)) {
            URL metadataUrl = URL.valueOf(metadataAddress);
            this.metadataCollector = ExtensionLoader.getLoader(
                    MetadataCollector.class).getExtension(metadataUrl.getProtocol());
            metadataCollector.initialize(metadataUrl);
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
        FullServiceDefinition fullServiceDefinition = serviceDefinitions.get(identifier.getIdentifierKey());
        if (fullServiceDefinition == null) {
            String metadata = metadataCollector.pullMetaData(identifier);
            System.out.println(metadata);
            if (StringUtils.isBlank(metadata)) {
                return;
            }

            fullServiceDefinition = JSON.parseObject(metadata, FullServiceDefinition.class);
            serviceDefinitions.put(identifier.getIdentifierKey(), fullServiceDefinition);
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

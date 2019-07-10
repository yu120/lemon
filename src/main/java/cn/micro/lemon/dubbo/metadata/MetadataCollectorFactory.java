package cn.micro.lemon.dubbo.metadata;

import cn.micro.lemon.dubbo.ServiceDefinition;
import com.alibaba.fastjson.JSON;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.metadata.definition.model.FullServiceDefinition;
import org.apache.dubbo.metadata.definition.model.MethodDefinition;
import org.apache.dubbo.metadata.identifier.MetadataIdentifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Metadata Collector Factory
 *
 * @author lry
 */
public class MetadataCollectorFactory {

    private MetadataCollector metadataCollector;

    public void initialize(String metadataAddress) {
        if (StringUtils.isNotEmpty(metadataAddress)) {
            URL metadataUrl = URL.valueOf(metadataAddress);
            //this.metadataCollector = ExtensionLoader.getExtensionLoader(MetadataCollector.class).getExtension(metadataUrl.getProtocol());
            this.metadataCollector = new ZookeeperMetadataCollector();
            metadataCollector.initialize(metadataUrl);
        }
    }

    public void wrapperTypesFromMetadata(ServiceDefinition serviceDefinition) {
        MetadataIdentifier identifier = new MetadataIdentifier(
                serviceDefinition.getServiceId(), serviceDefinition.getVersion(),
                serviceDefinition.getGroup(), CommonConstants.PROVIDER_SIDE, serviceDefinition.getApplication());
        String metadata = metadataCollector.getProviderMetaData(identifier);
        if (StringUtils.isBlank(metadata)) {
            return;
        }

        FullServiceDefinition fullServiceDefinition = JSON.parseObject(metadata, FullServiceDefinition.class);
        List<MethodDefinition> methods = fullServiceDefinition.getMethods();
        if (methods != null) {
            for (MethodDefinition m : methods) {
                if (sameMethod(m, serviceDefinition.getMethodName(), serviceDefinition.getParamValues().length)) {
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

    private static boolean sameMethod(MethodDefinition m, String methodName, int paramLen) {
        return (m.getName().equals(methodName) && m.getParameterTypes().length == paramLen);
    }

}

package cn.micro.lemon.dubbo;

import cn.micro.lemon.MicroConfig;
import cn.micro.lemon.dubbo.metadata.MetadataCollectorFactory;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.utils.ReferenceConfigCache;
import org.apache.dubbo.rpc.service.GenericService;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DubboInvokeProxy {

    private ApplicationConfig application;
    private RegistryConfig registry;
    private MetadataCollectorFactory metadataCollectorFactory;

    public void initialize(MicroConfig microConfig) {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName(microConfig.getApplication());
        this.application = applicationConfig;

        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(microConfig.getDubbo().getRegistryAddress());
        this.registry = registryConfig;

        this.metadataCollectorFactory = new MetadataCollectorFactory();
        metadataCollectorFactory.initialize(microConfig.getDubbo().getMetadataAddress());
    }

    public Object invoke(ServiceDefinition serviceDefinition) {
        ReferenceConfig<GenericService> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setApplication(application);
        referenceConfig.setRegistry(registry);
        referenceConfig.setInterface(serviceDefinition.getService());
        referenceConfig.setGeneric(true);

        if (serviceDefinition.getParamTypes() == null) {
            metadataCollectorFactory.wrapperTypesFromMetadata(serviceDefinition);
        }

        GenericService genericService = ReferenceConfigCache.getCache().get(referenceConfig);
        return genericService.$invoke(serviceDefinition.getMethodName(),
                serviceDefinition.getParamTypes(), serviceDefinition.getParamValues());
    }

    public CompletableFuture<Object> invokeAsync(String interfaceClass, String method,
                                                 List<String> paramTypes, List<Object> paramValues) {
        ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
        reference.setApplication(application);
        reference.setRegistry(registry);
        reference.setInterface(interfaceClass);
        reference.setGeneric(true);

        String[] invokeParamTypes = paramTypes.toArray(new String[0]);
        Object[] invokeParamValues = paramValues.toArray(new Object[0]);

        GenericService genericService = ReferenceConfigCache.getCache().get(reference);
        return genericService.$invokeAsync(method, invokeParamTypes, invokeParamValues);
    }

}
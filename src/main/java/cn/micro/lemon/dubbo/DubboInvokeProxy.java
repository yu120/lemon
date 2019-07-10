package cn.micro.lemon.dubbo;

import cn.micro.lemon.InvokeProxy;
import cn.micro.lemon.MicroConfig;
import cn.micro.lemon.dubbo.metadata.MetadataCollectorFactory;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.utils.ReferenceConfigCache;
import org.apache.dubbo.rpc.service.GenericService;

import java.util.concurrent.CompletableFuture;

public class DubboInvokeProxy implements InvokeProxy {

    private ApplicationConfig application;
    private RegistryConfig registry;
    private MetadataCollectorFactory metadataCollectorFactory;

    @Override
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

    @Override
    public Object invoke(ServiceDefinition serviceDefinition) {
        GenericService genericService = buildGenericService(serviceDefinition);
        return genericService.$invoke(serviceDefinition.getMethodName(),
                serviceDefinition.getParamTypes(), serviceDefinition.getParamValues());
    }

    @Override
    public CompletableFuture<Object> invokeAsync(ServiceDefinition serviceDefinition) {
        GenericService genericService = buildGenericService(serviceDefinition);
        return genericService.$invokeAsync(serviceDefinition.getMethodName(),
                serviceDefinition.getParamTypes(), serviceDefinition.getParamValues());
    }

    private GenericService buildGenericService(ServiceDefinition serviceDefinition) {
        ReferenceConfig<GenericService> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setApplication(application);
        referenceConfig.setRegistry(registry);
        referenceConfig.setInterface(serviceDefinition.getService());
        referenceConfig.setGeneric(true);

        if (serviceDefinition.getParamTypes() == null) {
            metadataCollectorFactory.wrapperTypesFromMetadata(serviceDefinition);
        }

        return ReferenceConfigCache.getCache().get(referenceConfig);
    }

}
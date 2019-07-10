package cn.micro.lemon.dubbo;

import cn.micro.lemon.InvokeProxy;
import cn.micro.lemon.MicroConfig;
import cn.micro.lemon.dubbo.metadata.MetadataCollectorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.utils.ReferenceConfigCache;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.service.GenericService;

import java.util.concurrent.CompletableFuture;

@Slf4j
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
        try {
            GenericService genericService = buildGenericService(serviceDefinition);
            log.debug("Dubbo generic invoke is starting, service definition is {}, generic service is {}.", serviceDefinition, genericService);
            Object result = genericService.$invoke(serviceDefinition.getMethod(),
                    serviceDefinition.getParamTypes(), serviceDefinition.getParamValues());
            log.debug("Dubbo generic invoke is end, service definition is {}, return result is {}.", serviceDefinition, result);
            return result;
        } catch (Exception e) {
            log.error("Dubbo generic invoke failed", e);
            if (e instanceof RpcException) {
                RpcException e1 = (RpcException) e;
                if (e1.isTimeout()) {
                    return ResultCode.TIMEOUT;
                }
                if (e1.isBiz()) {
                    return ResultCode.BIZ_ERROR;
                }
                if (e1.isNetwork()) {
                    return ResultCode.NETWORK_ERROR;
                }
                if (e1.isSerialization()) {
                    return ResultCode.SERIALIZATION;
                }
            }

            throw e;
        }

    }

    @Override
    public CompletableFuture<Object> invokeAsync(ServiceDefinition serviceDefinition) {
        GenericService genericService = buildGenericService(serviceDefinition);
        return genericService.$invokeAsync(serviceDefinition.getMethod(),
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
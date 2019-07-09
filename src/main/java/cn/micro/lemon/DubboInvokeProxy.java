package cn.micro.lemon;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.utils.ReferenceConfigCache;
import org.apache.dubbo.rpc.service.GenericService;

import java.util.*;

public class DubboInvokeProxy {

    private ApplicationConfig application;
    private RegistryConfig registry;

    public void initialize(String applicationName, String registryAddress) {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName(applicationName);
        this.application = applicationConfig;

        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(registryAddress);
        this.registry = registryConfig;
    }

    public Object invoke(String interfaceClass, String methodName, List<String> paramTypes, List<Object> paramValues) {
        ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
        reference.setApplication(application);
        reference.setRegistry(registry);
        reference.setInterface(interfaceClass);
        reference.setGeneric(true);

        String[] invokeParamTypes = paramTypes.toArray(new String[0]);
        Object[] invokeParamValues = paramValues.toArray(new Object[0]);

        GenericService genericService = ReferenceConfigCache.getCache().get(reference);
        return genericService.$invoke(methodName, invokeParamTypes, invokeParamValues);
    }

}
package cn.micro.lemon;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.utils.ReferenceConfigCache;
import org.apache.dubbo.rpc.service.GenericService;

import java.util.*;

public class DubboGateway {

    private ApplicationConfig application;
    private RegistryConfig registry;

    public static void main(String[] args) {
        String interfaceClass = "cn.micro.biz.dubbo.provider.DemoService";
        String methodName = "sayHello";
        List<String> paramTypes = new ArrayList<>();
        paramTypes.add("java.lang.String");
        List<Object> paramValues = new ArrayList<>();
        paramValues.add("张三");

        DubboGateway dubboGateway = new DubboGateway();
        dubboGateway.initialize();
        Object result = dubboGateway.invoke(interfaceClass, methodName, paramTypes, paramValues);
        System.out.println(result);
    }

    public void initialize() {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName("micro-dubbo-gateway");
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress("zookeeper://127.0.0.1:2181");
        this.application = applicationConfig;
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
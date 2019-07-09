package cn.micro.lemon;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.utils.ReferenceConfigCache;
import org.apache.dubbo.rpc.service.GenericService;

import java.util.*;

public enum DubboGateway {

    // ====

    INSTANCE;

    private ApplicationConfig application;
    private RegistryConfig registry;

    DubboGateway() {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName("micro-dubbo-gateway");
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress("zookeeper://127.0.0.1:2181");
        this.application = applicationConfig;
        this.registry = registryConfig;
}

    public static void main(String[] args) {
        List<Map<String, Object>> parameters = new ArrayList<>();
        Map<String, Object> tempParameters = new HashMap<>();
        tempParameters.put("ParamType", "java.lang.String");
        tempParameters.put("Object", "张三");
        parameters.add(tempParameters);

        Object result = DubboGateway.INSTANCE.invoke(
                "cn.micro.biz.dubbo.provider.DemoService",
                "sayHello",
                parameters);
        System.out.println(result);
    }

    public Object invoke(String interfaceClass, String methodName, List<Map<String, Object>> parameters) {
        ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
        reference.setApplication(application);
        reference.setRegistry(registry);
        reference.setInterface(interfaceClass);
        reference.setGeneric(true);

        //ReferenceConfig实例很重，封装了与注册中心的连接以及与提供者的连接，
        //需要缓存，否则重复生成ReferenceConfig可能造成性能问题并且会有内存和连接泄漏。
        //API方式编程时，容易忽略此问题。
        //这里使用dubbo内置的简单缓存工具类进行缓存

        ReferenceConfigCache cache = ReferenceConfigCache.getCache();
        GenericService genericService = cache.get(reference);

        // 用com.alibaba.dubbo.rpc.service.GenericService可以替代所有接口引用
        int len = parameters.size();
        String[] invokeParamTyeps = new String[len];
        Object[] invokeParams = new Object[len];
        for (int i = 0; i < len; i++) {
            invokeParamTyeps[i] = parameters.get(i).get("ParamType") + "";
            invokeParams[i] = parameters.get(i).get("Object");
        }

        return genericService.$invoke(methodName, invokeParamTyeps, invokeParams);
    }

}
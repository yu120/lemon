package cn.micro.lemon;

import cn.micro.lemon.dubbo.DubboConfig;
import cn.micro.lemon.dubbo.DubboInvokeProxy;
import cn.micro.lemon.dubbo.ServiceDefinition;

import java.util.*;

public class Demo {

    public static void main(String[] args) {
        String serviceId = "cn.micro.biz.dubbo.provider.DemoService";

        DubboConfig dubboConfig = new DubboConfig();
        dubboConfig.setRegistryAddress("zookeeper://127.0.0.1:2181");
        dubboConfig.setMetadataAddress("zookeeper://127.0.0.1:2181");

        MicroConfig microConfig = new MicroConfig();
        microConfig.setApplication("micro-dubbo-gateway");
        microConfig.setDubbo(dubboConfig);

        InvokeProxy invokeProxy = new DubboInvokeProxy();
        invokeProxy.initialize(microConfig);

        // 测试案例1
        ServiceDefinition serviceDefinition1 = new ServiceDefinition();
        serviceDefinition1.setApplication("micro-dubbo-provider");
        serviceDefinition1.setService(serviceId);
        serviceDefinition1.setMethod("sayHello");
        serviceDefinition1.setParamValues(new Object[]{"张三"});
        Object result1 = invokeProxy.invoke(serviceDefinition1);
        System.out.println("sayHello: " + result1);

        // 测试案例2
        Map<String, Object> map2 = new HashMap<>();
        map2.put("name", "张三");
        map2.put("age", 22);
        ServiceDefinition serviceDefinition2 = new ServiceDefinition();
        serviceDefinition2.setApplication("micro-dubbo-provider");
        serviceDefinition2.setService(serviceId);
        serviceDefinition2.setMethod("test");
        serviceDefinition2.setParamValues(new Object[]{map2});
        Object result2 = invokeProxy.invoke(serviceDefinition2);
        System.out.println("test: " + result2);

        // 测试案例3
        Map<String, Object> map3 = new HashMap<>();
        map3.put("name", "李四");
        map3.put("age", 33);
        List<Object> list3 = new ArrayList<>();
        list3.add(map2);
        list3.add(map3);
        ServiceDefinition serviceDefinition3 = new ServiceDefinition();
        serviceDefinition3.setApplication("micro-dubbo-provider");
        serviceDefinition3.setService(serviceId);
        serviceDefinition3.setMethod("demo");
        serviceDefinition3.setParamValues(new Object[]{list3});
        Object result3 = invokeProxy.invoke(serviceDefinition3);
        System.out.println("demo: " + result3);
    }

}

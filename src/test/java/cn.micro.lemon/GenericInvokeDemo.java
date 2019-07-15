package cn.micro.lemon;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.rpc.service.GenericService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericInvokeDemo {

    public static void main(String[] args) {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress("zookeeper://127.0.0.1:2181");

        ReferenceConfig<GenericService> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setApplication(new ApplicationConfig("micro-dubbo-gateway"));
        referenceConfig.setRegistry(registryConfig);
        referenceConfig.setInterface("cn.micro.biz.dubbo.provider.DemoService");
        referenceConfig.setGeneric(true);

        GenericService genericService = referenceConfig.get();
        Map<String, Object> person = new HashMap<>();
        person.put("name", "张三");
        person.put("age", 22);

        List<Object> list = new ArrayList<>();
        list.add(person);

        Object result = genericService.$invoke("demo", new String[]{"java.util.List"}, new Object[]{list});
        System.out.println(result);
    }

}

package cn.micro.lemon;

import java.util.*;

public class Demo {

    public static void main(String[] args) {
        String interfaceClass = "cn.micro.biz.dubbo.provider.DemoService";

        DubboInvokeProxy dubboInvokeProxy = new DubboInvokeProxy();
        dubboInvokeProxy.initialize("micro-dubbo-gateway", "zookeeper://127.0.0.1:2181");
        Object result1 = dubboInvokeProxy.invoke(interfaceClass, "sayHello",
                Arrays.asList("java.lang.String"),
                Arrays.asList("张三"));
        System.out.println("sayHello: " + result1);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("name", "张三");
        map2.put("age", 22);
        Object result2 = dubboInvokeProxy.invoke(interfaceClass, "test",
                Arrays.asList("cn.micro.biz.dubbo.provider.User"),
                Arrays.asList(map2));
        System.out.println("test: " + result2);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("name", "李四");
        map3.put("age", 33);
        List<Object> list3=new ArrayList<>();
        list3.add(map2);
        list3.add(map3);
        Object result3 = dubboInvokeProxy.invoke(interfaceClass, "demo",
                Arrays.asList("java.util.List"),
                Arrays.asList(list3));
        System.out.println("demo: " + result3);
    }

}

package cn.micro.lemon;

import java.util.ArrayList;
import java.util.List;

public class Demo {

    public static void main(String[] args) {
        String interfaceClass = "cn.micro.biz.dubbo.provider.DemoService";
        String methodName = "sayHello";
        List<String> paramTypes = new ArrayList<>();
        paramTypes.add("java.lang.String");
        List<Object> paramValues = new ArrayList<>();
        paramValues.add("张三");

        DubboInvokeProxy dubboInvokeProxy = new DubboInvokeProxy();
        dubboInvokeProxy.initialize("micro-dubbo-gateway", "zookeeper://127.0.0.1:2181");
        Object result = dubboInvokeProxy.invoke(interfaceClass, methodName, paramTypes, paramValues);
        System.out.println(result);
    }

}

package org.micro.lemon.dubbo;

import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.rpc.service.GenericService;

public class GenericConsumerApplication {

    public static void main(String[] args) {
        ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
        reference.setInterface("org.micro.lemon.dubbo.DemoService");
        reference.setVersion("1.0.0");
        reference.setGeneric(true);
        GenericService genericService = reference.get();
        Object result = genericService.$invoke("sayHello",
                new String[]{"java.lang.String"}, new Object[]{"world===="});
        System.out.println(result);
    }

}

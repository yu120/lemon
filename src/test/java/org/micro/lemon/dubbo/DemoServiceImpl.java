package org.micro.lemon.dubbo;

public class DemoServiceImpl implements DemoService {

    @Override
    public String sayHi(String name) {
        System.out.println("收到：" + name);
        return "hi, " + name;
    }

}
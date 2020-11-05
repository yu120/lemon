package org.micro.lemon.dubbo;

public class GreetingsServiceImpl implements GreetingsService {

    @Override
    public String sayHi(String name) {
        System.out.println("收到：" + name);
        return "hi, " + name;
    }

}
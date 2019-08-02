package org.micro.lemon.provider;

import org.apache.dubbo.config.annotation.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DemoServiceImpl implements DemoService {

    @Override
    public String sayHello(String name) {
        System.out.println("1===" + name);
        return "Hello " + name;
    }

    @Override
    public User test(User user) {
        System.out.println("2===" + user);
        return user;
    }

    @Override
    public List<User> testList(User user) {
        System.out.println("3===" + user);
        List<User> users = new ArrayList<>();
        users.add(user);
        return users;
    }

    @Override
    public List<User> demo(List<User> users) {
        System.out.println("4===" + users);
        return users;
    }

}
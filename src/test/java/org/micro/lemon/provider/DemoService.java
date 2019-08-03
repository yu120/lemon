package org.micro.lemon.provider;

import org.micro.lemon.proxy.dubbo.metadata.annotation.LemonService;

import java.util.List;

@LemonService
public interface DemoService {

    String sayHello(String name);

    User test(User user);

    List<User> testList(User user);

    List<User> demo(List<User> users);

}
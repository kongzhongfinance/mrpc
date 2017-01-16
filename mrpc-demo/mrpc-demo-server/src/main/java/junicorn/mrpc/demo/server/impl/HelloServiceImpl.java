package junicorn.mrpc.demo.server.impl;

import junicorn.mrpc.demo.api.HelloService;
import junicorn.mrpc.demo.model.Person;
import junicorn.mrpc.spring.annotation.MRpcService;

@MRpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String name) {
        System.out.println("name = " + name);
        return "Hello! " + name;
    }

    @Override
    public String hello(Person person) {
        return "Hello! " + person.getName() + " " + person.getPhoneNumber();
    }
}

package junicorn.mrpc.demo.api;

import junicorn.mrpc.demo.model.Person;

public interface HelloService {

    String hello(String name);

    String hello(Person person);
}

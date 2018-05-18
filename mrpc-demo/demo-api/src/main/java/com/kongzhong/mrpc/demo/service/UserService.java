package com.kongzhong.mrpc.demo.service;

import com.kongzhong.mrpc.annotation.Command;
import com.kongzhong.mrpc.annotation.Comment;
import com.kongzhong.mrpc.demo.exception.BizException;
import com.kongzhong.mrpc.demo.exception.NoArgException;
import com.kongzhong.mrpc.demo.model.Person;
import com.kongzhong.mrpc.demo.model.Result;
import com.kongzhong.mrpc.demo.model.StatusEnum;
import com.kongzhong.mrpc.demo.model.XXDto;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author biezhi
 * 2017/4/19
 */
@Comment(name = "用户服务", owners = "biezhi", emails = "biezhi.me@gmail.com")
public interface UserService {

    default int add(int a, int b) {
        return a + b;
    }

    default String hello(String name) {
        try {
            TimeUnit.MILLISECONDS.sleep(new Random().nextInt(30));
        } catch (InterruptedException e) {
        }
        return "Hello, " + name;
    }

    default Person savePerson(String fullName, Integer age) {
        Person person = new Person();
        person.setName(fullName);
        return person;
    }

    default Person save(Person person) {
        return person;
    }

    default Long delete(Long id) {
        return id;
    }

    default List<String> strList(List<String> strs) {
        return strs;
    }

    default List<Person> getPersons() {
        Person person = new Person();
        person.setName("jack");
        List<Person> list = new ArrayList<>();
        list.add(person);
        return list;
    }

    default Result<Person> getResult() {
        Result<Person> result = new Result<>();
        result.setData(new Person("test1"));
        return result;
    }

    default void setPersons(@NotEmpty(message = "参数不能为空") List<Person> persons) {
        System.out.println(persons);
    }

    default Map toMap(Map<String, Integer> map) {
        return map;
    }

    default void testArray(String[] strs) {
        System.out.println("接收到：" + Arrays.toString(strs));
    }

    default void testBizExp(Integer num) throws BizException {
        throw new BizException(1200, "xx对象不能为空");
    }

    default void testNoArgException() {
        throw new NoArgException(22, "没有构造函数的异常");
    }

    default void testNormalExp() throws Exception {
        throw new Exception("我是一个异常");
    }

    default StatusEnum testEnum(StatusEnum statusEnum) {
        System.out.println("接收到枚举：" + statusEnum);
        return statusEnum;
    }

    default String testTimeout(int timeOut) {
        try {
            TimeUnit.SECONDS.sleep(timeOut);
            System.out.println("执行完毕");
            return "haha";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Command(fallbackType = "com.kongzhong.mrpc.client.UserServiceFallback", fallbackMethod = "fall")
    default String testHystrix(int num) {
        if (num != 20) {
            throw new RuntimeException("运行时异常");
        }
        return "ok";
    }

    String testTrace();

    default XXDto transDate(XXDto time) {
        System.out.println(time);
        return time;
    }

    default void testError() {
        System.out.println("test-------");
        throw new ExceptionInInitializerError("omg.");
    }

    String testServerCustomException();

    default String testRestart() {
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Hello";
    }

    default Optional<XXDto> testOptional() {
        XXDto xxDto = new XXDto();
        xxDto.setAge(22);
        return Optional.ofNullable(xxDto);
    }
}
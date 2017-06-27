package com.kongzhong.mrpc.demo.service;

import com.kongzhong.mrpc.demo.exception.BizException;
import com.kongzhong.mrpc.demo.model.Person;
import com.kongzhong.mrpc.demo.model.Result;
import com.kongzhong.mrpc.demo.model.StatusEnum;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author biezhi
 *         2017/4/19
 */
public interface UserService {

    int add(int a, int b);

    String hello(String name);

    Person savePerson(String fullName, Integer age);

    Person save(Person person);

    Long delete(Long id);

    List<String> strList(List<String> strs);

    List<Person> getPersons();

    Result<Person> getResult();

    void setPersons(@NotEmpty(message = "参数不能为空") List<Person> persons);

    Map toMap(Map<String, Integer> map);

    void testArray(String[] strs);

    void testBizExp(Integer num) throws BizException;

    void testNormalExp() throws Exception;

    StatusEnum testEnum(StatusEnum statusEnum);

    default void testTimeout(int timeOut) {
        try {
            TimeUnit.SECONDS.sleep(timeOut);
            System.out.println("执行完毕");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
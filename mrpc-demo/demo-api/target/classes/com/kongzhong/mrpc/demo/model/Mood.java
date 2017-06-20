package com.kongzhong.mrpc.demo.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Mood implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id = "1111111111111";
    private Integer age = 2222;
    private String names[] = new String[]{"123123123", "123123123", "123123123123123"};

    private List<String> values = new LinkedList<String>();

    public Mood() {
        super();
        values.add("1111111111");
        values.add("1111111111");
        values.add("1111111111");
        values.add("1111111111");
        values.add("1111111111");
        values.add("1111111111");
        values.add("1111111111");
        values.add("1111111111");
        values.add("1111111111");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String[] getNames() {
        return names;
    }

    public void setNames(String[] names) {
        this.names = names;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "Mood [id=" + id + ", age=" + age + ", names=" + Arrays.toString(names) + ", values=" + values + "]";
    }

}

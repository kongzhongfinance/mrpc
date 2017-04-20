package com.kongzhong.mrpc.test.model;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

public class FullName implements Serializable {
    private String firstName;
    private String lastName;

    public FullName(String lastName, String firstName) {
        this.lastName = lastName;
        this.firstName = firstName;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
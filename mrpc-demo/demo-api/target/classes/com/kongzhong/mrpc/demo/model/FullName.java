package com.kongzhong.mrpc.demo.model;

import lombok.ToString;

import java.io.Serializable;

@ToString
public class FullName implements Serializable {
    private String firstName;
    private String lastName;

    public FullName(String lastName, String firstName) {
        this.lastName = lastName;
        this.firstName = firstName;
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
package com.kongzhong.mrpc.demo.model;

import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

@ToString
@NoArgsConstructor
public class FullName implements Serializable {
    @NotBlank(message = "姓不能为空")
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
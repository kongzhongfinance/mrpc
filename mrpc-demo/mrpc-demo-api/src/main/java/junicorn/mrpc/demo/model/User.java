package junicorn.mrpc.demo.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class User implements Serializable {

    private Long uid;
    private int age;
    private String name;
    private Date birthday;
    private BigDecimal money;

    public User() {
    }

    public User(Long uid, int age, String name, Date birthday, BigDecimal money) {
        this.uid = uid;
        this.age = age;
        this.name = name;
        this.birthday = birthday;
        this.money = money;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid=" + uid +
                ", age=" + age +
                ", name='" + name + '\'' +
                ", birthday=" + birthday +
                ", money=" + money +
                '}';
    }
}

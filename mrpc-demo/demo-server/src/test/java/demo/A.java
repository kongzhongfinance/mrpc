package demo;

import java.io.Serializable;

/**
 * @author biezhi
 *         2017/4/26
 */
public class A implements Serializable {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

package com.kongzhong.mrpc.trace.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author biezhi
 * @date 2017/11/29
 */
public class Exclusions {

    private Set<String> exclusions = new HashSet<>();

    public static Exclusions defaultExclusions() {
        Exclusions exclusions = new Exclusions();
        exclusions.add("*.js");
        exclusions.add("*.css");
        exclusions.add("*.html");
        exclusions.add("*.png");
        exclusions.add("*.jpg");
        exclusions.add("*.jpeg");
        exclusions.add("*.gif");
        exclusions.add("*.bmp");
        exclusions.add("*.mp3");
        exclusions.add("*.mp4");
        exclusions.add("*.flv");
        exclusions.add("*.ico");
        exclusions.add("*.woff");
        exclusions.add("*.woff2");
        return exclusions;
    }

    public Exclusions add(String exclusion) {
        this.exclusions.add(exclusion);
        return this;
    }

    public Exclusions remove(String exclusion) {
        this.exclusions.remove(exclusion);
        return this;
    }

    public Exclusions clean() {
        this.exclusions.clear();
        return this;
    }

    @Override
    public String toString() {
        return this.exclusions.stream().collect(Collectors.joining(","));
    }

}

package com.kongzhong.mrpc.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;

@Data
@EqualsAndHashCode
@ToString
public class Foo {

    private Bar bar;
    private Map<Integer, String> map;

}
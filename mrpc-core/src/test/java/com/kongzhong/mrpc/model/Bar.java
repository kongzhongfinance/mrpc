package com.kongzhong.mrpc.model;

import lombok.*;

@Data
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Bar {

    private int id;
    private String name;

}
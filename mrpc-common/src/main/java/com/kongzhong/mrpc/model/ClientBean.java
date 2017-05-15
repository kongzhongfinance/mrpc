package com.kongzhong.mrpc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 客户端引用Bean
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ClientBean {

    private String id;
    private String interfaceName;

}
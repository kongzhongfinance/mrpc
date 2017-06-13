package com.kongzhong.mrpc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author biezhi
 *         2017/6/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StackTrace implements Serializable {

    private String declaringClass;
    private String methodName;
    private String fileName;
    private int lineNumber;

}

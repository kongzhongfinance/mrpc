package com.kongzhong.mrpc.demo.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by biezhi on 12/07/2017.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ParentException extends RuntimeException {

    protected String pid;

}

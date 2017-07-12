package com.kongzhong.mrpc.demo.exception;

import lombok.Data;

/**
 * Created by biezhi on 12/07/2017.
 */
@Data
public class ParentException extends RuntimeException {

    protected String pid;

}

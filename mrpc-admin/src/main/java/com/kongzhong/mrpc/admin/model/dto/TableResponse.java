package com.kongzhong.mrpc.admin.model.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by biezhi on 01/07/2017.
 */
@Data
public class TableResponse<T> {

    private long total;
    private List<T> rows;

}

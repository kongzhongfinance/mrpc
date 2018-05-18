package com.kongzhong.mrpc.admin.model;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PageList<T> {

    private List<T> rows;
    private long total;

    public static <T> PageList<T> convert(Page<T> page){
        PageList<T> pageList = new PageList<>();
        pageList.setRows(page.getContent());
        pageList.setTotal(page.getTotalElements());
        return pageList;
    }
}

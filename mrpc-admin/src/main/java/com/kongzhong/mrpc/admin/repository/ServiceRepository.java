package com.kongzhong.mrpc.admin.repository;

import com.kongzhong.mrpc.admin.model.entity.ServiceEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by biezhi on 11/07/2017.
 */
public interface ServiceRepository extends PagingAndSortingRepository<ServiceEntity, String> {

}

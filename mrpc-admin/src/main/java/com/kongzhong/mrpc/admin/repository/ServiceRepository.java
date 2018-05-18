package com.kongzhong.mrpc.admin.repository;

import com.kongzhong.mrpc.admin.model.entity.ServiceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by biezhi on 11/07/2017.
 */
public interface ServiceRepository extends JpaSpecificationExecutor<ServiceEntity>,
        PagingAndSortingRepository<ServiceEntity, String>, CrudRepository<ServiceEntity, String> {

}

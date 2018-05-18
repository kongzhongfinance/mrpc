package com.kongzhong.mrpc.admin.repository;

import com.kongzhong.mrpc.admin.model.entity.ServiceNodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by biezhi on 11/07/2017.
 */
public interface RelateRepository extends JpaRepository<ServiceNodeEntity, Long> {

}

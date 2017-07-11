package com.kongzhong.mrpc.admin.repository;

import com.kongzhong.mrpc.admin.model.entity.NodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by biezhi on 11/07/2017.
 */
public interface NodeRepository extends JpaRepository<NodeEntity, String> {

}

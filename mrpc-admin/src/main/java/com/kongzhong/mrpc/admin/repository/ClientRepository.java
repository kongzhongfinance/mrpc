package com.kongzhong.mrpc.admin.repository;

import com.kongzhong.mrpc.admin.model.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by biezhi on 11/07/2017.
 */
public interface ClientRepository extends JpaRepository<ClientEntity, Long> {

}

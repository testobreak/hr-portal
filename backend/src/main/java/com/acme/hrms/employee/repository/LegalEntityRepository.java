package com.acme.hrms.employee.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.acme.hrms.employee.entity.LegalEntity;

@Repository
public interface LegalEntityRepository
        extends JpaRepository<LegalEntity, UUID>, JpaSpecificationExecutor<LegalEntity> {

    Optional<LegalEntity> findByCodeIgnoreCase(String code);
}

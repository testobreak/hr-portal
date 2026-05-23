package com.acme.hrms.employee.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.acme.hrms.employee.entity.Designation;

@Repository
public interface DesignationRepository
        extends JpaRepository<Designation, UUID>, JpaSpecificationExecutor<Designation> {

    Optional<Designation> findByTitleIgnoreCase(String title);
}

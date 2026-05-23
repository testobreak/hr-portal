package com.acme.hrms.employee.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.acme.hrms.employee.entity.Location;

@Repository
public interface LocationRepository
        extends JpaRepository<Location, UUID>, JpaSpecificationExecutor<Location> {

    Optional<Location> findByCodeIgnoreCase(String code);
}

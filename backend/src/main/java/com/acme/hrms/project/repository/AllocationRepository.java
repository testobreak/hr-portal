package com.acme.hrms.project.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.acme.hrms.project.entity.Allocation;

@Repository
public interface AllocationRepository extends JpaRepository<Allocation, UUID>, JpaSpecificationExecutor<Allocation> {
}

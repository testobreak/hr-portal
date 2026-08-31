package com.acme.hrms.recruitment.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.acme.hrms.recruitment.entity.JobRequisition;

@Repository
public interface JobRequisitionRepository extends JpaRepository<JobRequisition, UUID> {
}

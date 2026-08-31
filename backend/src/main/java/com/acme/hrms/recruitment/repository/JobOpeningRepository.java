package com.acme.hrms.recruitment.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.acme.hrms.recruitment.entity.JobOpening;

@Repository
public interface JobOpeningRepository extends JpaRepository<JobOpening, UUID> {
    java.util.Optional<JobOpening> findByJobRequisitionId(UUID jobRequisitionId);
}

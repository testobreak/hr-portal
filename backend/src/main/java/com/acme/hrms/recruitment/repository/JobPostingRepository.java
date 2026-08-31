package com.acme.hrms.recruitment.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.acme.hrms.recruitment.entity.JobPosting;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, UUID> {
    Optional<JobPosting> findByPublicId(UUID publicId);
    java.util.List<JobPosting> findAllByStatus(String status);
}

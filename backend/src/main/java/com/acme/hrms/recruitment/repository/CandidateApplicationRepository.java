package com.acme.hrms.recruitment.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.acme.hrms.recruitment.entity.CandidateApplication;

@Repository
public interface CandidateApplicationRepository extends JpaRepository<CandidateApplication, UUID> {
    List<CandidateApplication> findByJobOpeningId(UUID jobOpeningId);
}

package com.acme.hrms.onboarding.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.acme.hrms.onboarding.entity.PreHire;

@Repository
public interface PreHireRepository extends JpaRepository<PreHire, UUID> {
    Optional<PreHire> findByCandidateId(UUID candidateId);
    Optional<PreHire> findByAcceptedOfferId(UUID acceptedOfferId);
}

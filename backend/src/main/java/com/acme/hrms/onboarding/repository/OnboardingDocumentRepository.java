package com.acme.hrms.onboarding.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.acme.hrms.onboarding.entity.OnboardingDocument;

@Repository
public interface OnboardingDocumentRepository extends JpaRepository<OnboardingDocument, UUID> {
    List<OnboardingDocument> findByPreHireId(UUID preHireId);
}

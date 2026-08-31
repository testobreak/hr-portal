package com.acme.hrms.onboarding.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.acme.hrms.onboarding.entity.OnboardingPlan;

@Repository
public interface OnboardingPlanRepository extends JpaRepository<OnboardingPlan, UUID> {
    Optional<OnboardingPlan> findByPreHireId(UUID preHireId);
}

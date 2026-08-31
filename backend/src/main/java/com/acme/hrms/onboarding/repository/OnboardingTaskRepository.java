package com.acme.hrms.onboarding.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.acme.hrms.onboarding.entity.OnboardingTask;

@Repository
public interface OnboardingTaskRepository extends JpaRepository<OnboardingTask, UUID> {
    List<OnboardingTask> findByOnboardingPlanId(UUID onboardingPlanId);
}

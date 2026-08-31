package com.acme.hrms.onboarding.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.acme.hrms.onboarding.entity.BackgroundCheck;

@Repository
public interface BackgroundCheckRepository extends JpaRepository<BackgroundCheck, UUID> {
    List<BackgroundCheck> findByPreHireId(UUID preHireId);
}

package com.acme.hrms.onboarding.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.acme.hrms.onboarding.entity.AssetRequest;

@Repository
public interface AssetRequestRepository extends JpaRepository<AssetRequest, UUID> {
    List<AssetRequest> findByPreHireId(UUID preHireId);
}

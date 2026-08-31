package com.acme.hrms.profile.repository;

import com.acme.hrms.profile.entity.ProfileChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProfileChangeRequestRepository extends JpaRepository<ProfileChangeRequest, UUID> {
    List<ProfileChangeRequest> findByEmployeeId(UUID employeeId);
}

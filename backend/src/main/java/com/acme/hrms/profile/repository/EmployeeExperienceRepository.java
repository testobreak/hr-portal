package com.acme.hrms.profile.repository;

import com.acme.hrms.profile.entity.EmployeeExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface EmployeeExperienceRepository extends JpaRepository<EmployeeExperience, UUID> {
    List<EmployeeExperience> findByEmployeeId(UUID employeeId);
}

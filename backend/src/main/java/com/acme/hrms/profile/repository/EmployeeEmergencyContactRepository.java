package com.acme.hrms.profile.repository;

import com.acme.hrms.profile.entity.EmployeeEmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface EmployeeEmergencyContactRepository extends JpaRepository<EmployeeEmergencyContact, UUID> {
    List<EmployeeEmergencyContact> findByEmployeeId(UUID employeeId);
}

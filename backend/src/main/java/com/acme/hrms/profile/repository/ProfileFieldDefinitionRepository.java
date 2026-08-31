package com.acme.hrms.profile.repository;

import com.acme.hrms.profile.entity.ProfileFieldDefinition;
import com.acme.hrms.profile.entity.ProfileFieldDefinitionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileFieldDefinitionRepository extends JpaRepository<ProfileFieldDefinition, ProfileFieldDefinitionId> {
}

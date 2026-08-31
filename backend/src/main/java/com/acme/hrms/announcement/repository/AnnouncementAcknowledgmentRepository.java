package com.acme.hrms.announcement.repository;

import com.acme.hrms.announcement.entity.AnnouncementAcknowledgment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnnouncementAcknowledgmentRepository extends JpaRepository<AnnouncementAcknowledgment, UUID> {
    Optional<AnnouncementAcknowledgment> findByAnnouncementIdAndEmployeeId(UUID announcementId, UUID employeeId);
}

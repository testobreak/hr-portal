package com.acme.hrms.announcement.repository;

import com.acme.hrms.announcement.entity.AnnouncementDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface AnnouncementDeliveryRepository extends JpaRepository<AnnouncementDelivery, UUID> {
    List<AnnouncementDelivery> findByEmployeeId(UUID employeeId);
    List<AnnouncementDelivery> findByAnnouncementId(UUID announcementId);
}

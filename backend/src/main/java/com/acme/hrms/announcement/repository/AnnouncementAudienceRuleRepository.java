package com.acme.hrms.announcement.repository;

import com.acme.hrms.announcement.entity.AnnouncementAudienceRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface AnnouncementAudienceRuleRepository extends JpaRepository<AnnouncementAudienceRule, UUID> {
    List<AnnouncementAudienceRule> findByAnnouncementId(UUID announcementId);
}

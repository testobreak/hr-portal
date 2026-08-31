package com.acme.hrms.announcement.service;

import com.acme.hrms.announcement.entity.Announcement;
import java.util.List;
import java.util.UUID;

public interface AnnouncementService {
    Announcement publishAnnouncement(String title, String content, UUID authorId, UUID legalEntityId, UUID departmentId, UUID locationId);
    List<Announcement> getMyAnnouncements(UUID employeeId);
    void markAsRead(UUID employeeId, UUID announcementId);
    void acknowledge(UUID employeeId, UUID announcementId);
}

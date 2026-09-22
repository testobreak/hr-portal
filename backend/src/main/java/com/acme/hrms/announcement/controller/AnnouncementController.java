package com.acme.hrms.announcement.controller;

import com.acme.hrms.announcement.entity.Announcement;
import com.acme.hrms.announcement.service.AnnouncementService;
import com.acme.hrms.common.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.acme.hrms.common.error.NotFoundException;
import com.acme.hrms.employee.repository.EmployeeRepository;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final EmployeeRepository employeeRepository;

    public AnnouncementController(AnnouncementService announcementService,
                                  EmployeeRepository employeeRepository) {
        this.announcementService = announcementService;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/me/announcements")
    public ResponseEntity<List<Announcement>> getMyAnnouncements() {
        UUID subjectUuid = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElse(null);
        if (subjectUuid == null || !employeeRepository.existsById(subjectUuid)) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(announcementService.getMyAnnouncements(subjectUuid));
    }

    @PostMapping("/me/announcements/{announcementId}/read")
    public ResponseEntity<Void> read(@PathVariable UUID announcementId) {
        UUID subjectUuid = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElseThrow(() -> new IllegalStateException("User not found in security context"));
        UUID myId = subjectUuid;
        if (!employeeRepository.existsById(myId)) {
            throw NotFoundException.of("Employee", myId);
        }
        announcementService.markAsRead(myId, announcementId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/me/announcements/{announcementId}/acknowledge")
    public ResponseEntity<Void> acknowledge(@PathVariable UUID announcementId) {
        UUID subjectUuid = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElseThrow(() -> new IllegalStateException("User not found in security context"));
        UUID myId = subjectUuid;
        if (!employeeRepository.existsById(myId)) {
            throw NotFoundException.of("Employee", myId);
        }
        announcementService.acknowledge(myId, announcementId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/announcements")
    public ResponseEntity<Announcement> publish(
            @RequestParam(name = "title") String title,
            @RequestParam(name = "content") String content,
            @RequestParam(name = "legalEntityId", required = false) UUID legalEntityId,
            @RequestParam(name = "departmentId", required = false) UUID departmentId,
            @RequestParam(name = "locationId", required = false) UUID locationId) {
        UUID subjectUuid = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElseThrow(() -> new IllegalStateException("User not found in security context"));
        UUID authorId = subjectUuid;
        if (!employeeRepository.existsById(authorId)) {
            throw NotFoundException.of("Employee", authorId);
        }
        Announcement ann = announcementService.publishAnnouncement(title, content, authorId, legalEntityId, departmentId, locationId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ann);
    }
}

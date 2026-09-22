package com.acme.hrms.announcement.service;

import com.acme.hrms.announcement.entity.*;
import com.acme.hrms.announcement.repository.*;
import com.acme.hrms.employee.entity.Employee;
import com.acme.hrms.employee.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final AnnouncementAudienceRuleRepository ruleRepository;
    private final AnnouncementDeliveryRepository deliveryRepository;
    private final AnnouncementAcknowledgmentRepository ackRepository;
    private final EmployeeRepository employeeRepository;

    public AnnouncementServiceImpl(AnnouncementRepository announcementRepository,
                                   AnnouncementAudienceRuleRepository ruleRepository,
                                   AnnouncementDeliveryRepository deliveryRepository,
                                   AnnouncementAcknowledgmentRepository ackRepository,
                                   EmployeeRepository employeeRepository) {
        this.announcementRepository = announcementRepository;
        this.ruleRepository = ruleRepository;
        this.deliveryRepository = deliveryRepository;
        this.ackRepository = ackRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public Announcement publishAnnouncement(String title, String content, UUID authorId, UUID legalEntityId, UUID departmentId, UUID locationId) {
        Employee author = employeeRepository.findById(authorId)
                .orElseThrow(() -> com.acme.hrms.common.error.NotFoundException.of("Employee", authorId));

        Announcement ann = Announcement.builder()
                .title(title)
                .content(content)
                .publishedAt(Instant.now())
                .status("PUBLISHED")
                .authorId(authorId)
                .pinned(false)
                .build();
        ann.setTenantId(author.getTenantId());
        Announcement saved = announcementRepository.save(ann);

        AnnouncementAudienceRule rule = AnnouncementAudienceRule.builder()
                .announcementId(saved.getId())
                .legalEntityId(legalEntityId)
                .departmentId(departmentId)
                .locationId(locationId)
                .build();
        rule.setTenantId(author.getTenantId());
        ruleRepository.save(rule);

        // Project deliveries immediately
        List<Employee> allEmployees = employeeRepository.findAll().stream()
                .filter(e -> author.getTenantId().equals(e.getTenantId()))
                .collect(Collectors.toList());

        for (Employee emp : allEmployees) {
            if (legalEntityId != null && (emp.getLegalEntity() == null || !legalEntityId.equals(emp.getLegalEntity().getId()))) {
                continue;
            }
            if (departmentId != null && (emp.getDepartment() == null || !departmentId.equals(emp.getDepartment().getId()))) {
                continue;
            }
            if (locationId != null && (emp.getLocation() == null || !locationId.equals(emp.getLocation().getId()))) {
                continue;
            }

            AnnouncementDelivery del = AnnouncementDelivery.builder()
                    .announcementId(saved.getId())
                    .employeeId(emp.getId())
                    .deliveredAt(Instant.now())
                    .build();
            del.setTenantId(author.getTenantId());
            deliveryRepository.save(del);
        }

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Announcement> getMyAnnouncements(UUID employeeId) {
        List<AnnouncementDelivery> deliveries = deliveryRepository.findByEmployeeId(employeeId);
        List<Announcement> list = new ArrayList<>();
        for (AnnouncementDelivery d : deliveries) {
            announcementRepository.findById(d.getAnnouncementId()).ifPresent(list::add);
        }
        return list;
    }

    @Override
    @Transactional
    public void markAsRead(UUID employeeId, UUID announcementId) {
        List<AnnouncementDelivery> dels = deliveryRepository.findByAnnouncementId(announcementId).stream()
                .filter(d -> employeeId.equals(d.getEmployeeId()))
                .collect(Collectors.toList());

        for (AnnouncementDelivery d : dels) {
            if (d.getReadAt() == null) {
                d.setReadAt(Instant.now());
                deliveryRepository.save(d);
            }
        }
    }

    @Override
    @Transactional
    public void acknowledge(UUID employeeId, UUID announcementId) {
        Optional<AnnouncementAcknowledgment> existing = ackRepository.findByAnnouncementIdAndEmployeeId(announcementId, employeeId);
        if (existing.isPresent()) return;

        Announcement ann = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new com.acme.hrms.common.error.NotFoundException("Announcement not found"));

        AnnouncementAcknowledgment ack = AnnouncementAcknowledgment.builder()
                .announcementId(announcementId)
                .employeeId(employeeId)
                .acknowledgedAt(Instant.now())
                .build();
        ack.setTenantId(ann.getTenantId());
        ackRepository.save(ack);
    }
}

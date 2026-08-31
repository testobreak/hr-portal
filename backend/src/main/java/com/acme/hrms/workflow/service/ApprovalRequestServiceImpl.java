package com.acme.hrms.workflow.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.hrms.common.error.ConflictException;
import com.acme.hrms.common.error.NotFoundException;
import com.acme.hrms.common.outbox.OutboxService;
import com.acme.hrms.common.audit.AuditAction;
import com.acme.hrms.common.audit.AuditEvent;
import com.acme.hrms.common.audit.AuditService;
import com.acme.hrms.manager.service.ManagerService;
import com.acme.hrms.leave.service.LeaveRequestService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.acme.hrms.common.security.CurrentUser;
import com.acme.hrms.employee.entity.Department;
import com.acme.hrms.employee.entity.Designation;
import com.acme.hrms.employee.entity.Employee;
import com.acme.hrms.employee.entity.Location;
import com.acme.hrms.employee.repository.DepartmentRepository;
import com.acme.hrms.employee.repository.DesignationRepository;
import com.acme.hrms.employee.repository.EmployeeRepository;
import com.acme.hrms.employee.repository.LocationRepository;
import com.acme.hrms.employee.service.EmployeeAssignmentHistoryService;
import com.acme.hrms.workflow.dto.ApprovalRequestCreateRequest;
import com.acme.hrms.workflow.dto.ApprovalRequestResponse;
import com.acme.hrms.workflow.dto.EmployeeAssignmentChangedEvent;
import com.acme.hrms.workflow.dto.EmployeeChangeRequest;
import com.acme.hrms.workflow.entity.ApprovalRequest;
import com.acme.hrms.workflow.entity.ApprovalStatus;
import com.acme.hrms.workflow.repository.ApprovalRequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.acme.hrms.recruitment.repository.JobRequisitionRepository;
import com.acme.hrms.recruitment.repository.JobOpeningRepository;
import com.acme.hrms.recruitment.repository.OfferRepository;
import com.acme.hrms.recruitment.entity.JobRequisition;
import com.acme.hrms.recruitment.entity.JobOpening;
import com.acme.hrms.recruitment.entity.Offer;

@Service
public class ApprovalRequestServiceImpl implements ApprovalRequestService {

    private static final Logger log = LoggerFactory.getLogger(ApprovalRequestServiceImpl.class);

    @org.springframework.beans.factory.annotation.Autowired
    private JobRequisitionRepository jobRequisitionRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private JobOpeningRepository jobOpeningRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private OfferRepository offerRepository;

    private final ApprovalRequestRepository repository;
    private final EmployeeRepository employees;
    private final DepartmentRepository departments;
    private final DesignationRepository designations;
    private final LocationRepository locations;
    private final EmployeeAssignmentHistoryService assignmentHistoryService;
    private final OutboxService outboxService;
    private final AuditService auditService;
    private final ManagerService managerService;
    private final LeaveRequestService leaveRequestService;
    private final ObjectMapper objectMapper;

    public ApprovalRequestServiceImpl(ApprovalRequestRepository repository,
                                     EmployeeRepository employees,
                                     DepartmentRepository departments,
                                     DesignationRepository designations,
                                     LocationRepository locations,
                                     EmployeeAssignmentHistoryService assignmentHistoryService,
                                     OutboxService outboxService,
                                     AuditService auditService,
                                     ManagerService managerService,
                                     LeaveRequestService leaveRequestService,
                                     ObjectMapper objectMapper) {
        this.repository = repository;
        this.employees = employees;
        this.departments = departments;
        this.designations = designations;
        this.locations = locations;
        this.assignmentHistoryService = assignmentHistoryService;
        this.outboxService = outboxService;
        this.auditService = auditService;
        this.managerService = managerService;
        this.leaveRequestService = leaveRequestService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ApprovalRequestResponse create(ApprovalRequestCreateRequest request) {
        // Enforce employee existence check
        employees.findById(request.employeeId())
                .orElseThrow(() -> NotFoundException.of("Employee", request.employeeId()));

        UUID requesterId = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElse(UUID.fromString("00000000-0000-0000-0000-000000000000"));

        ApprovalRequest entity = ApprovalRequest.builder()
                .requesterId(requesterId)
                .employeeId(request.employeeId())
                .type(request.type())
                .changeJson(request.changeJson())
                .status(ApprovalStatus.PENDING)
                .build();

        ApprovalRequest saved = repository.save(entity);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ApprovalRequestResponse approve(UUID id) {
        ApprovalRequest request = repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("ApprovalRequest", id));

        if (request.getStatus() != ApprovalStatus.PENDING) {
            throw new ConflictException("ApprovalRequest is not in PENDING status");
        }

        if ("JOB_REQUISITION".equals(request.getType())) {
            UUID jobRequisitionId;
            try {
                Map<String, String> map = objectMapper.readValue(request.getChangeJson(), new TypeReference<Map<String, String>>() {});
                jobRequisitionId = UUID.fromString(map.get("jobRequisitionId"));
            } catch (Exception e) {
                throw new ConflictException("Failed to parse job requisition change json: " + e.getMessage());
            }

            JobRequisition req = jobRequisitionRepository.findById(jobRequisitionId)
                    .orElseThrow(() -> NotFoundException.of("JobRequisition", jobRequisitionId));

            if ("PENDING_APPROVAL".equals(req.getStatus())) {
                req.setStatus("APPROVED");
                jobRequisitionRepository.save(req);

                // Auto-create active JobOpening
                JobOpening opening = JobOpening.builder()
                        .jobRequisition(req)
                        .status("OPEN")
                        .build();
                opening.setTenantId(req.getTenantId());
                jobOpeningRepository.save(opening);
            }

            UUID approverId = CurrentUser.fromSecurityContext()
                    .map(CurrentUser::subjectUuid)
                    .orElse(UUID.fromString("00000000-0000-0000-0000-000000000000"));

            request.setStatus(ApprovalStatus.APPROVED);
            request.setApprovedBy(approverId);
            request.setApprovedAt(Instant.now());
            ApprovalRequest saved = repository.save(request);

            log.info("NOTIFICATION SENT: Job Requisition {} approved by admin {}", jobRequisitionId, approverId);
            return toResponse(saved);
        }

        if ("OFFER_APPROVAL".equals(request.getType())) {
            UUID offerId;
            try {
                Map<String, String> map = objectMapper.readValue(request.getChangeJson(), new TypeReference<Map<String, String>>() {});
                offerId = UUID.fromString(map.get("offerId"));
            } catch (Exception e) {
                throw new ConflictException("Failed to parse offer approval change json: " + e.getMessage());
            }

            Offer offer = offerRepository.findById(offerId)
                    .orElseThrow(() -> NotFoundException.of("Offer", offerId));

            if ("PENDING_APPROVAL".equals(offer.getStatus()) || "DRAFT".equals(offer.getStatus())) {
                offer.setStatus("APPROVED");
                offerRepository.save(offer);
            }

            UUID approverId = CurrentUser.fromSecurityContext()
                    .map(CurrentUser::subjectUuid)
                    .orElse(UUID.fromString("00000000-0000-0000-0000-000000000000"));

            request.setStatus(ApprovalStatus.APPROVED);
            request.setApprovedBy(approverId);
            request.setApprovedAt(Instant.now());
            ApprovalRequest saved = repository.save(request);

            log.info("NOTIFICATION SENT: Offer {} approved by admin {}", offerId, approverId);
            return toResponse(saved);
        }

        if ("LEAVE_REQUEST".equals(request.getType())) {
            UUID leaveRequestId;
            try {
                Map<String, String> map = objectMapper.readValue(request.getChangeJson(), new TypeReference<Map<String, String>>() {});
                leaveRequestId = UUID.fromString(map.get("leaveRequestId"));
            } catch (Exception e) {
                throw new ConflictException("Failed to parse leave change json: " + e.getMessage());
            }

            leaveRequestService.onWorkflowComplete(leaveRequestId, "APPROVED");

            UUID approverId = CurrentUser.fromSecurityContext()
                    .map(CurrentUser::subjectUuid)
                    .orElse(UUID.fromString("00000000-0000-0000-0000-000000000000"));

            request.setStatus(ApprovalStatus.APPROVED);
            request.setApprovedBy(approverId);
            request.setApprovedAt(Instant.now());
            ApprovalRequest saved = repository.save(request);

            log.info("NOTIFICATION SENT: Leave request {} approved by admin {}", leaveRequestId, approverId);
            return toResponse(saved);
        }

        Employee emp = employees.findById(request.getEmployeeId())
                .orElseThrow(() -> NotFoundException.of("Employee", request.getEmployeeId()));

        EmployeeChangeRequest change;
        try {
            change = objectMapper.readValue(request.getChangeJson(), EmployeeChangeRequest.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse change JSON from request", e);
        }

        // Apply changes to employee
        if (change.departmentId() != null) {
            Department d = departments.findById(change.departmentId())
                    .orElseThrow(() -> NotFoundException.of("Department", change.departmentId()));
            emp.setDepartment(d);
        }
        if (change.designationId() != null) {
            Designation d = designations.findById(change.designationId())
                    .orElseThrow(() -> NotFoundException.of("Designation", change.designationId()));
            emp.setDesignation(d);
        }
        if (change.locationId() != null) {
            Location l = locations.findById(change.locationId())
                    .orElseThrow(() -> NotFoundException.of("Location", change.locationId()));
            emp.setLocation(l);
        }
        if (change.managerId() != null) {
            Employee m = employees.findById(change.managerId())
                    .orElseThrow(() -> NotFoundException.of("Employee", change.managerId()));
            emp.setManager(m);
        }

        Employee savedEmp = employees.save(emp);
        managerService.rebuildHierarchy(savedEmp);
        auditService.record(AuditEvent.of(AuditAction.UPDATE, "employee")
                .withEntityId(savedEmp.getId())
                .withDetail("Approved change request " + request.getId()));

        LocalDate effectiveFrom = change.effectiveFrom() != null ? LocalDate.parse(change.effectiveFrom()) : LocalDate.now();
        assignmentHistoryService.recordAssignment(emp, effectiveFrom);

        UUID approverId = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElse(UUID.fromString("00000000-0000-0000-0000-000000000000"));

        request.setStatus(ApprovalStatus.APPROVED);
        request.setApprovedBy(approverId);
        request.setApprovedAt(Instant.now());
        ApprovalRequest saved = repository.save(request);

        // Stage Outbox Event
        outboxService.stageEvent("EmployeeAssignmentChanged", new EmployeeAssignmentChangedEvent(
                emp.getId(),
                change.departmentId(),
                change.designationId(),
                change.locationId(),
                change.managerId(),
                effectiveFrom.toString()
        ));

        // Dispatch simulated notification alert
        log.info("NOTIFICATION SENT: Employee assignment changes for {} approved by manager {}. Effective date: {}", 
                emp.getId(), approverId, effectiveFrom);

        return toResponse(saved);
    }

    @Override
    @Transactional
    public ApprovalRequestResponse reject(UUID id) {
        ApprovalRequest request = repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("ApprovalRequest", id));

        if (request.getStatus() != ApprovalStatus.PENDING) {
            throw new ConflictException("ApprovalRequest is not in PENDING status");
        }

        if ("JOB_REQUISITION".equals(request.getType())) {
            UUID jobRequisitionId;
            try {
                Map<String, String> map = objectMapper.readValue(request.getChangeJson(), new TypeReference<Map<String, String>>() {});
                jobRequisitionId = UUID.fromString(map.get("jobRequisitionId"));
            } catch (Exception e) {
                throw new ConflictException("Failed to parse job requisition change json: " + e.getMessage());
            }

            JobRequisition req = jobRequisitionRepository.findById(jobRequisitionId)
                    .orElseThrow(() -> NotFoundException.of("JobRequisition", jobRequisitionId));

            if ("PENDING_APPROVAL".equals(req.getStatus())) {
                req.setStatus("REJECTED");
                jobRequisitionRepository.save(req);
            }

            UUID approverId = CurrentUser.fromSecurityContext()
                    .map(CurrentUser::subjectUuid)
                    .orElse(UUID.fromString("00000000-0000-0000-0000-000000000000"));

            request.setStatus(ApprovalStatus.REJECTED);
            request.setApprovedBy(approverId);
            request.setApprovedAt(Instant.now());
            ApprovalRequest saved = repository.save(request);

            log.info("NOTIFICATION SENT: Job Requisition {} rejected by admin {}", jobRequisitionId, approverId);
            return toResponse(saved);
        }

        if ("OFFER_APPROVAL".equals(request.getType())) {
            UUID offerId;
            try {
                Map<String, String> map = objectMapper.readValue(request.getChangeJson(), new TypeReference<Map<String, String>>() {});
                offerId = UUID.fromString(map.get("offerId"));
            } catch (Exception e) {
                throw new ConflictException("Failed to parse offer approval change json: " + e.getMessage());
            }

            Offer offer = offerRepository.findById(offerId)
                    .orElseThrow(() -> NotFoundException.of("Offer", offerId));

            if ("PENDING_APPROVAL".equals(offer.getStatus())) {
                offer.setStatus("REJECTED");
                offerRepository.save(offer);
            }

            UUID approverId = CurrentUser.fromSecurityContext()
                    .map(CurrentUser::subjectUuid)
                    .orElse(UUID.fromString("00000000-0000-0000-0000-000000000000"));

            request.setStatus(ApprovalStatus.REJECTED);
            request.setApprovedBy(approverId);
            request.setApprovedAt(Instant.now());
            ApprovalRequest saved = repository.save(request);

            log.info("NOTIFICATION SENT: Offer {} rejected by admin {}", offerId, approverId);
            return toResponse(saved);
        }

        if ("LEAVE_REQUEST".equals(request.getType())) {
            UUID leaveRequestId;
            try {
                Map<String, String> map = objectMapper.readValue(request.getChangeJson(), new TypeReference<Map<String, String>>() {});
                leaveRequestId = UUID.fromString(map.get("leaveRequestId"));
            } catch (Exception e) {
                throw new ConflictException("Failed to parse leave change json: " + e.getMessage());
            }

            leaveRequestService.onWorkflowComplete(leaveRequestId, "REJECTED");

            UUID approverId = CurrentUser.fromSecurityContext()
                    .map(CurrentUser::subjectUuid)
                    .orElse(UUID.fromString("00000000-0000-0000-0000-000000000000"));

            request.setStatus(ApprovalStatus.REJECTED);
            request.setApprovedBy(approverId);
            request.setApprovedAt(Instant.now());
            ApprovalRequest saved = repository.save(request);

            log.info("NOTIFICATION SENT: Leave request {} rejected by admin {}", leaveRequestId, approverId);
            return toResponse(saved);
        }

        UUID approverId = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElse(UUID.fromString("00000000-0000-0000-0000-000000000000"));

        request.setStatus(ApprovalStatus.REJECTED);
        request.setApprovedBy(approverId);
        request.setApprovedAt(Instant.now());
        ApprovalRequest saved = repository.save(request);

        log.info("NOTIFICATION SENT: Employee change request {} rejected by admin {}", id, approverId);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalRequestResponse get(UUID id) {
        ApprovalRequest request = repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("ApprovalRequest", id));
        return toResponse(request);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<ApprovalRequestResponse> getPendingRequests() {
        return repository.findAllByStatus(ApprovalStatus.PENDING).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ApprovalRequestResponse toResponse(ApprovalRequest entity) {
        return new ApprovalRequestResponse(
                entity.getId(),
                entity.getRequesterId(),
                entity.getEmployeeId(),
                entity.getType(),
                entity.getChangeJson(),
                entity.getStatus().name(),
                entity.getApprovedBy(),
                entity.getApprovedAt(),
                entity.getCreatedAt(),
                entity.getVersion()
        );
    }
}

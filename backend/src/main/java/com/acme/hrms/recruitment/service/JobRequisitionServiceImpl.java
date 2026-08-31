package com.acme.hrms.recruitment.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.hrms.common.error.ConflictException;
import com.acme.hrms.common.error.NotFoundException;
import com.acme.hrms.common.security.CurrentUser;
import com.acme.hrms.employee.entity.Department;
import com.acme.hrms.employee.entity.Designation;
import com.acme.hrms.employee.entity.Employee;
import com.acme.hrms.employee.entity.LegalEntity;
import com.acme.hrms.employee.entity.Location;
import com.acme.hrms.employee.repository.DepartmentRepository;
import com.acme.hrms.employee.repository.DesignationRepository;
import com.acme.hrms.employee.repository.EmployeeRepository;
import com.acme.hrms.employee.repository.LegalEntityRepository;
import com.acme.hrms.employee.repository.LocationRepository;
import com.acme.hrms.recruitment.dto.JobRequisitionCreateRequest;
import com.acme.hrms.recruitment.dto.JobRequisitionResponse;
import com.acme.hrms.recruitment.entity.JobOpening;
import com.acme.hrms.recruitment.entity.JobRequisition;
import com.acme.hrms.recruitment.repository.JobOpeningRepository;
import com.acme.hrms.recruitment.repository.JobRequisitionRepository;
import com.acme.hrms.workflow.entity.ApprovalRequest;
import com.acme.hrms.workflow.entity.ApprovalStatus;
import com.acme.hrms.workflow.repository.ApprovalRequestRepository;

@Service
public class JobRequisitionServiceImpl implements JobRequisitionService {

    private final JobRequisitionRepository repository;
    private final JobOpeningRepository openingRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final LocationRepository locationRepository;
    private final LegalEntityRepository legalEntityRepository;
    private final ApprovalRequestRepository approvalRequestRepository;

    public JobRequisitionServiceImpl(JobRequisitionRepository repository,
                                     JobOpeningRepository openingRepository,
                                     EmployeeRepository employeeRepository,
                                     DepartmentRepository departmentRepository,
                                     DesignationRepository designationRepository,
                                     LocationRepository locationRepository,
                                     LegalEntityRepository legalEntityRepository,
                                     ApprovalRequestRepository approvalRequestRepository) {
        this.repository = repository;
        this.openingRepository = openingRepository;
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.designationRepository = designationRepository;
        this.locationRepository = locationRepository;
        this.legalEntityRepository = legalEntityRepository;
        this.approvalRequestRepository = approvalRequestRepository;
    }

    @Override
    @Transactional
    public JobRequisitionResponse create(JobRequisitionCreateRequest request) {
        String reqNumber = "REQ-" + (System.currentTimeMillis() % 10000000);

        JobRequisition req = JobRequisition.builder()
                .reqNumber(reqNumber)
                .jobTitle(request.jobTitle())
                .department(request.departmentId() != null ? departmentRepository.findById(request.departmentId()).orElse(null) : null)
                .designation(request.designationId() != null ? designationRepository.findById(request.designationId()).orElse(null) : null)
                .location(request.locationId() != null ? locationRepository.findById(request.locationId()).orElse(null) : null)
                .legalEntity(request.legalEntityId() != null ? legalEntityRepository.findById(request.legalEntityId()).orElse(null) : null)
                .employmentType(request.employmentType())
                .openingsCount(request.openingsCount() != null ? request.openingsCount() : 1)
                .hiringManager(request.hiringManagerId() != null ? employeeRepository.findById(request.hiringManagerId()).orElse(null) : null)
                .targetStartDate(request.targetStartDate())
                .minSalary(request.minSalary())
                .maxSalary(request.maxSalary())
                .currencyCode(request.currencyCode() != null ? request.currencyCode() : "USD")
                .requiredSkills(request.requiredSkills())
                .minExperienceYears(request.minExperienceYears())
                .description(request.description())
                .justification(request.justification())
                .status("DRAFT")
                .build();

        JobRequisition saved = repository.save(req);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobRequisitionResponse> listAll() {
        return repository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public JobRequisitionResponse getById(UUID id) {
        JobRequisition req = repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("JobRequisition", id));
        return toResponse(req);
    }

    @Override
    @Transactional
    public JobRequisitionResponse update(UUID id, JobRequisitionCreateRequest request) {
        JobRequisition req = repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("JobRequisition", id));

        if (!"DRAFT".equals(req.getStatus())) {
            throw new ConflictException("Cannot edit requisition in status: " + req.getStatus());
        }

        req.setJobTitle(request.jobTitle());
        req.setDepartment(request.departmentId() != null ? departmentRepository.findById(request.departmentId()).orElse(null) : null);
        req.setDesignation(request.designationId() != null ? designationRepository.findById(request.designationId()).orElse(null) : null);
        req.setLocation(request.locationId() != null ? locationRepository.findById(request.locationId()).orElse(null) : null);
        req.setLegalEntity(request.legalEntityId() != null ? legalEntityRepository.findById(request.legalEntityId()).orElse(null) : null);
        req.setEmploymentType(request.employmentType());
        req.setOpeningsCount(request.openingsCount() != null ? request.openingsCount() : 1);
        req.setHiringManager(request.hiringManagerId() != null ? employeeRepository.findById(request.hiringManagerId()).orElse(null) : null);
        req.setTargetStartDate(request.targetStartDate());
        req.setMinSalary(request.minSalary());
        req.setMaxSalary(request.maxSalary());
        req.setCurrencyCode(request.currencyCode() != null ? request.currencyCode() : "USD");
        req.setRequiredSkills(request.requiredSkills());
        req.setMinExperienceYears(request.minExperienceYears());
        req.setDescription(request.description());
        req.setJustification(request.justification());

        JobRequisition saved = repository.save(req);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void submit(UUID id) {
        JobRequisition req = repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("JobRequisition", id));

        if (!"DRAFT".equals(req.getStatus())) {
            throw new ConflictException("Only DRAFT requisitions can be submitted");
        }

        req.setStatus("PENDING_APPROVAL");
        repository.save(req);

        UUID requesterId = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElse(UUID.fromString("00000000-0000-0000-0000-000000000000"));

        ApprovalRequest approvalRequest = ApprovalRequest.builder()
                .employeeId(req.getHiringManager() != null ? req.getHiringManager().getId() : requesterId)
                .requesterId(requesterId)
                .type("JOB_REQUISITION")
                .changeJson("{\"jobRequisitionId\":\"" + id + "\"}")
                .status(ApprovalStatus.PENDING)
                .build();
        approvalRequest.setTenantId(req.getTenantId());
        approvalRequestRepository.save(approvalRequest);
    }

    @Override
    @Transactional
    public void approve(UUID id) {
        JobRequisition req = repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("JobRequisition", id));

        if (!"PENDING_APPROVAL".equals(req.getStatus())) {
            return; // Idempotent or ignored
        }

        req.setStatus("APPROVED");
        repository.save(req);

        // Auto-create active JobOpening
        JobOpening opening = JobOpening.builder()
                .jobRequisition(req)
                .status("OPEN")
                .build();
        opening.setTenantId(req.getTenantId());
        openingRepository.save(opening);
    }

    @Override
    @Transactional
    public void reject(UUID id) {
        JobRequisition req = repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("JobRequisition", id));

        if (!"PENDING_APPROVAL".equals(req.getStatus())) {
            return;
        }

        req.setStatus("REJECTED");
        repository.save(req);
    }

    @Override
    @Transactional
    public void close(UUID id) {
        JobRequisition req = repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("JobRequisition", id));
        req.setStatus("CLOSED");
        repository.save(req);
    }

    private JobRequisitionResponse toResponse(JobRequisition entity) {
        UUID jobOpeningId = openingRepository.findByJobRequisitionId(entity.getId())
                .map(JobOpening::getId)
                .orElse(null);

        return new JobRequisitionResponse(
                entity.getId(),
                jobOpeningId,
                entity.getReqNumber(),
                entity.getJobTitle(),
                entity.getDepartment() != null ? entity.getDepartment().getId() : null,
                entity.getDepartment() != null ? entity.getDepartment().getName() : null,
                entity.getDesignation() != null ? entity.getDesignation().getId() : null,
                entity.getDesignation() != null ? entity.getDesignation().getTitle() : null,
                entity.getLocation() != null ? entity.getLocation().getId() : null,
                entity.getLocation() != null ? entity.getLocation().getName() : null,
                entity.getLegalEntity() != null ? entity.getLegalEntity().getId() : null,
                entity.getLegalEntity() != null ? entity.getLegalEntity().getName() : null,
                entity.getEmploymentType(),
                entity.getOpeningsCount(),
                entity.getHiringManager() != null ? entity.getHiringManager().getId() : null,
                entity.getHiringManager() != null ? (entity.getHiringManager().getFirstName() + " " + entity.getHiringManager().getLastName()) : null,
                entity.getRecruiter() != null ? entity.getRecruiter().getId() : null,
                entity.getRecruiter() != null ? (entity.getRecruiter().getFirstName() + " " + entity.getRecruiter().getLastName()) : null,
                entity.getTargetStartDate(),
                entity.getMinSalary(),
                entity.getMaxSalary(),
                entity.getCurrencyCode(),
                entity.getRequiredSkills(),
                entity.getMinExperienceYears(),
                entity.getDescription(),
                entity.getJustification(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }
}

package com.acme.hrms.onboarding.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.hrms.common.error.ConflictException;
import com.acme.hrms.common.error.NotFoundException;
import com.acme.hrms.employee.dto.EmployeeCreateRequest;
import com.acme.hrms.employee.dto.EmployeeResponse;
import com.acme.hrms.employee.entity.EmploymentStatus;
import com.acme.hrms.employee.service.EmployeeService;
import com.acme.hrms.onboarding.dto.EmployeeActivationRequest;
import com.acme.hrms.onboarding.dto.OnboardingDocumentResponse;
import com.acme.hrms.onboarding.dto.OnboardingPlanResponse;
import com.acme.hrms.onboarding.dto.OnboardingTaskResponse;
import com.acme.hrms.onboarding.dto.PreHireResponse;
import com.acme.hrms.onboarding.dto.BackgroundCheckResponse;
import com.acme.hrms.onboarding.dto.AssetRequestResponse;
import com.acme.hrms.onboarding.entity.OnboardingPlan;
import com.acme.hrms.onboarding.entity.OnboardingDocument;
import com.acme.hrms.onboarding.entity.OnboardingTask;
import com.acme.hrms.onboarding.entity.PreHire;
import com.acme.hrms.onboarding.entity.BackgroundCheck;
import com.acme.hrms.onboarding.entity.AssetRequest;
import com.acme.hrms.onboarding.repository.OnboardingPlanRepository;
import com.acme.hrms.onboarding.repository.OnboardingDocumentRepository;
import com.acme.hrms.onboarding.repository.OnboardingTaskRepository;
import com.acme.hrms.onboarding.repository.PreHireRepository;
import com.acme.hrms.onboarding.repository.BackgroundCheckRepository;
import com.acme.hrms.onboarding.repository.AssetRequestRepository;

@Service
public class OnboardingServiceImpl implements OnboardingService {

    private final PreHireRepository preHireRepository;
    private final OnboardingPlanRepository planRepository;
    private final OnboardingTaskRepository taskRepository;
    private final OnboardingDocumentRepository documentRepository;
    private final BackgroundCheckRepository backgroundCheckRepository;
    private final AssetRequestRepository assetRequestRepository;
    private final EmployeeService employeeService;

    public OnboardingServiceImpl(PreHireRepository preHireRepository,
                                 OnboardingPlanRepository planRepository,
                                 OnboardingTaskRepository taskRepository,
                                 OnboardingDocumentRepository documentRepository,
                                 BackgroundCheckRepository backgroundCheckRepository,
                                 AssetRequestRepository assetRequestRepository,
                                 EmployeeService employeeService) {
        this.preHireRepository = preHireRepository;
        this.planRepository = planRepository;
        this.taskRepository = taskRepository;
        this.documentRepository = documentRepository;
        this.backgroundCheckRepository = backgroundCheckRepository;
        this.assetRequestRepository = assetRequestRepository;
        this.employeeService = employeeService;
    }

    @Override
    @Transactional(readOnly = true)
    public PreHireResponse getPreHire(UUID id) {
        PreHire preHire = preHireRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("PreHire", id));
        return toPreHireResponse(preHire);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PreHireResponse> listPreHires() {
        return preHireRepository.findAll().stream()
                .map(this::toPreHireResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OnboardingPlanResponse getOnboardingPlan(UUID preHireId) {
        OnboardingPlan plan = planRepository.findByPreHireId(preHireId)
                .orElseThrow(() -> new NotFoundException("Onboarding plan not found for Pre-Hire ID " + preHireId));
        List<OnboardingTask> tasks = taskRepository.findByOnboardingPlanId(plan.getId());
        return toPlanResponse(plan, tasks);
    }

    @Override
    @Transactional
    public OnboardingPlanResponse createOnboardingPlan(UUID preHireId, String templateName) {
        PreHire preHire = preHireRepository.findById(preHireId)
                .orElseThrow(() -> NotFoundException.of("PreHire", preHireId));

        planRepository.findByPreHireId(preHireId).ifPresent(p -> {
            throw new ConflictException("Onboarding plan already exists for Pre-Hire ID " + preHireId);
        });

        OnboardingPlan plan = OnboardingPlan.builder()
                .preHire(preHire)
                .templateName(templateName)
                .build();
        plan.setTenantId(preHire.getTenantId());
        OnboardingPlan savedPlan = planRepository.save(plan);

        // Generate standard tasks
        List<OnboardingTask> tasks = new ArrayList<>();
        LocalDate baseDueDate = preHire.getStartDate().minusDays(2);

        tasks.add(createTaskEntity(savedPlan, "Submit Identification Document", "Upload government-issued photo ID", "CANDIDATE", baseDueDate));
        tasks.add(createTaskEntity(savedPlan, "Submit Signed Contract", "Upload signed employment terms agreement", "CANDIDATE", baseDueDate));
        tasks.add(createTaskEntity(savedPlan, "Conduct Background Verification", "Verify education and employment references", "HR", baseDueDate));
        tasks.add(createTaskEntity(savedPlan, "Provision Laptop and Accounts", "Prepare corporate device and email active credentials", "IT", baseDueDate.plusDays(1)));
        tasks.add(createTaskEntity(savedPlan, "Prepare Week 1 Induction Agenda", "Align team calls, syncs, and introductory training materials", "MANAGER", baseDueDate.plusDays(1)));

        List<OnboardingTask> savedTasks = taskRepository.saveAll(tasks);

        // Also request standard documents in database
        requestDocumentInternal(preHire, "GOVERNMENT_ID");
        requestDocumentInternal(preHire, "SIGNED_CONTRACT");

        preHire.setStatus("ONBOARDING_IN_PROGRESS");
        preHireRepository.save(preHire);

        return toPlanResponse(savedPlan, savedTasks);
    }

    private OnboardingTask createTaskEntity(OnboardingPlan plan, String name, String desc, String role, LocalDate due) {
        OnboardingTask task = OnboardingTask.builder()
                .onboardingPlan(plan)
                .taskName(name)
                .description(desc)
                .assignedRole(role)
                .status("NOT_STARTED")
                .dueDate(due)
                .build();
        task.setTenantId(plan.getTenantId());
        return task;
    }

    private void requestDocumentInternal(PreHire preHire, String docType) {
        OnboardingDocument doc = OnboardingDocument.builder()
                .preHire(preHire)
                .documentType(docType)
                .status("REQUESTED")
                .build();
        doc.setTenantId(preHire.getTenantId());
        documentRepository.save(doc);
    }

    @Override
    @Transactional
    public OnboardingTaskResponse updateTaskStatus(UUID taskId, String status) {
        OnboardingTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> NotFoundException.of("OnboardingTask", taskId));

        if (!List.of("NOT_STARTED", "IN_PROGRESS", "COMPLETED", "WAIVED").contains(status)) {
            throw new ConflictException("Invalid task status: " + status);
        }

        task.setStatus(status);
        OnboardingTask saved = taskRepository.save(task);

        // Check if all tasks are complete
        OnboardingPlan plan = saved.getOnboardingPlan();
        List<OnboardingTask> allTasks = taskRepository.findByOnboardingPlanId(plan.getId());
        boolean allDone = allTasks.stream().allMatch(t -> "COMPLETED".equals(t.getStatus()) || "WAIVED".equals(t.getStatus()));

        PreHire preHire = plan.getPreHire();
        if (allDone && "ONBOARDING_IN_PROGRESS".equals(preHire.getStatus())) {
            preHire.setStatus("READY_FOR_ACTIVATION");
            preHireRepository.save(preHire);
        }

        return toTaskResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OnboardingDocumentResponse> listDocuments(UUID preHireId) {
        return documentRepository.findByPreHireId(preHireId).stream()
                .map(this::toDocResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OnboardingDocumentResponse requestDocument(UUID preHireId, String documentType) {
        PreHire preHire = preHireRepository.findById(preHireId)
                .orElseThrow(() -> NotFoundException.of("PreHire", preHireId));

        OnboardingDocument doc = OnboardingDocument.builder()
                .preHire(preHire)
                .documentType(documentType)
                .status("REQUESTED")
                .build();
        doc.setTenantId(preHire.getTenantId());

        OnboardingDocument saved = documentRepository.save(doc);
        return toDocResponse(saved);
    }

    @Override
    @Transactional
    public OnboardingDocumentResponse uploadDocument(UUID docId, String storageKey) {
        OnboardingDocument doc = documentRepository.findById(docId)
                .orElseThrow(() -> NotFoundException.of("OnboardingDocument", docId));
        doc.setStorageKey(storageKey);
        doc.setStatus("UPLOADED");
        OnboardingDocument saved = documentRepository.save(doc);
        return toDocResponse(saved);
    }

    @Override
    @Transactional
    public OnboardingDocumentResponse reviewDocument(UUID docId, String status) {
        OnboardingDocument doc = documentRepository.findById(docId)
                .orElseThrow(() -> NotFoundException.of("OnboardingDocument", docId));

        if (!List.of("ACCEPTED", "REJECTED").contains(status)) {
            throw new ConflictException("Invalid document review status: " + status);
        }

        doc.setStatus(status);
        OnboardingDocument saved = documentRepository.save(doc);
        return toDocResponse(saved);
    }

    @Override
    @Transactional
    public EmployeeResponse activatePreHire(UUID preHireId, EmployeeActivationRequest request) {
        PreHire preHire = preHireRepository.findById(preHireId)
                .orElseThrow(() -> NotFoundException.of("PreHire", preHireId));

        if ("ACTIVATED".equals(preHire.getStatus())) {
            throw new ConflictException("Pre-Hire candidate is already activated as an active Employee.");
        }

        // Trigger Employee Creation
        EmployeeCreateRequest createRequest = new EmployeeCreateRequest(
                request.employeeCode(),
                preHire.getCandidate().getFirstName(),
                preHire.getCandidate().getLastName(),
                preHire.getCandidate().getEmail(),
                request.phoneNumber() != null ? request.phoneNumber() : preHire.getCandidate().getPhone(),
                request.dateOfBirth(),
                preHire.getStartDate(),
                EmploymentStatus.ACTIVE,
                null,
                preHire.getDepartment().getId(),
                preHire.getDesignation().getId(),
                preHire.getLocation().getId(),
                preHire.getLegalEntity().getId(),
                preHire.getManager() != null ? preHire.getManager().getId() : null,
                null,
                null
        );

        EmployeeResponse activeEmployee = employeeService.create(createRequest);

        preHire.setStatus("ACTIVATED");
        preHireRepository.save(preHire);

        return activeEmployee;
    }

    private PreHireResponse toPreHireResponse(PreHire entity) {
        String candidateName = entity.getCandidate().getFirstName() + " " + entity.getCandidate().getLastName();
        String candidateEmail = entity.getCandidate().getEmail();
        String legalEntityName = entity.getLegalEntity().getName();
        String departmentName = entity.getDepartment().getName();
        String designationTitle = entity.getDesignation().getTitle();
        String locationName = entity.getLocation().getName();
        String managerName = entity.getManager() != null ?
                entity.getManager().getFirstName() + " " + entity.getManager().getLastName() : "—";

        return new PreHireResponse(
                entity.getId(),
                entity.getCandidate().getId(),
                candidateName,
                candidateEmail,
                entity.getAcceptedOffer().getId(),
                legalEntityName,
                departmentName,
                designationTitle,
                locationName,
                managerName,
                entity.getStartDate(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }

    private OnboardingPlanResponse toPlanResponse(OnboardingPlan plan, List<OnboardingTask> tasks) {
        List<OnboardingTaskResponse> taskResponses = tasks.stream()
                .map(this::toTaskResponse)
                .collect(Collectors.toList());

        return new OnboardingPlanResponse(
                plan.getId(),
                plan.getPreHire().getId(),
                plan.getTemplateName(),
                taskResponses,
                plan.getCreatedAt(),
                plan.getUpdatedAt(),
                plan.getVersion()
        );
    }

    private OnboardingTaskResponse toTaskResponse(OnboardingTask task) {
        return new OnboardingTaskResponse(
                task.getId(),
                task.getOnboardingPlan().getId(),
                task.getTaskName(),
                task.getDescription(),
                task.getAssignedRole(),
                task.getStatus(),
                task.getDueDate(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getVersion()
        );
    }

    private OnboardingDocumentResponse toDocResponse(OnboardingDocument doc) {
        return new OnboardingDocumentResponse(
                doc.getId(),
                doc.getPreHire().getId(),
                doc.getDocumentType(),
                doc.getStorageKey(),
                doc.getStatus(),
                doc.getCreatedAt(),
                doc.getUpdatedAt(),
                doc.getVersion()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<BackgroundCheckResponse> listBackgroundChecks(UUID preHireId) {
        return backgroundCheckRepository.findByPreHireId(preHireId).stream()
                .map(this::toBackgroundCheckResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BackgroundCheckResponse triggerBackgroundCheck(UUID preHireId) {
        PreHire preHire = preHireRepository.findById(preHireId)
                .orElseThrow(() -> NotFoundException.of("PreHire", preHireId));

        BackgroundCheck check = BackgroundCheck.builder()
                .preHire(preHire)
                .status("IN_PROGRESS")
                .build();
        check.setTenantId(preHire.getTenantId());

        BackgroundCheck saved = backgroundCheckRepository.save(check);
        return toBackgroundCheckResponse(saved);
    }

    @Override
    @Transactional
    public BackgroundCheckResponse updateBackgroundCheckStatus(UUID checkId, String status) {
        BackgroundCheck check = backgroundCheckRepository.findById(checkId)
                .orElseThrow(() -> NotFoundException.of("BackgroundCheck", checkId));

        if (!List.of("NOT_STARTED", "IN_PROGRESS", "CLEARED", "FAILED").contains(status)) {
            throw new ConflictException("Invalid background check status: " + status);
        }

        check.setStatus(status);
        BackgroundCheck saved = backgroundCheckRepository.save(check);
        return toBackgroundCheckResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetRequestResponse> listAssetRequests(UUID preHireId) {
        return assetRequestRepository.findByPreHireId(preHireId).stream()
                .map(this::toAssetRequestResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AssetRequestResponse requestAsset(UUID preHireId, String assetType) {
        PreHire preHire = preHireRepository.findById(preHireId)
                .orElseThrow(() -> NotFoundException.of("PreHire", preHireId));

        AssetRequest asset = AssetRequest.builder()
                .preHire(preHire)
                .assetType(assetType)
                .status("REQUESTED")
                .build();
        asset.setTenantId(preHire.getTenantId());

        AssetRequest saved = assetRequestRepository.save(asset);
        return toAssetRequestResponse(saved);
    }

    @Override
    @Transactional
    public AssetRequestResponse updateAssetStatus(UUID assetId, String status) {
        AssetRequest asset = assetRequestRepository.findById(assetId)
                .orElseThrow(() -> NotFoundException.of("AssetRequest", assetId));

        if (!List.of("REQUESTED", "RESERVED", "DELIVERED").contains(status)) {
            throw new ConflictException("Invalid asset status: " + status);
        }

        asset.setStatus(status);
        AssetRequest saved = assetRequestRepository.save(asset);
        return toAssetRequestResponse(saved);
    }

    private BackgroundCheckResponse toBackgroundCheckResponse(BackgroundCheck check) {
        return new BackgroundCheckResponse(
                check.getId(),
                check.getPreHire().getId(),
                check.getStatus(),
                check.getCreatedAt(),
                check.getUpdatedAt(),
                check.getVersion()
        );
    }

    private AssetRequestResponse toAssetRequestResponse(AssetRequest asset) {
        return new AssetRequestResponse(
                asset.getId(),
                asset.getPreHire().getId(),
                asset.getAssetType(),
                asset.getStatus(),
                asset.getCreatedAt(),
                asset.getUpdatedAt(),
                asset.getVersion()
        );
    }
}

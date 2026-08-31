package com.acme.hrms.profile.service;

import com.acme.hrms.common.audit.AuditAction;
import com.acme.hrms.common.audit.AuditEvent;
import com.acme.hrms.common.audit.AuditService;
import com.acme.hrms.common.error.ConflictException;
import com.acme.hrms.common.error.NotFoundException;
import com.acme.hrms.common.security.CurrentUser;
import com.acme.hrms.employee.entity.Employee;
import com.acme.hrms.employee.repository.EmployeeRepository;
import com.acme.hrms.profile.dto.ProfileChangeRequestResponse;
import com.acme.hrms.profile.dto.ProfileResponse;
import com.acme.hrms.profile.entity.*;
import com.acme.hrms.profile.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final EmployeeRepository employeeRepository;
    private final ProfileFieldDefinitionRepository fieldRepository;
    private final ProfileChangeRequestRepository changeRequestRepository;
    private final EmployeeEmergencyContactRepository contactRepository;
    private final EmployeeDependentRepository dependentRepository;
    private final EmployeeEducationRepository educationRepository;
    private final EmployeeExperienceRepository experienceRepository;
    private final EmployeeSkillRepository skillRepository;
    private final EmployeeCertificationRepository certificationRepository;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public ProfileServiceImpl(EmployeeRepository employeeRepository,
                              ProfileFieldDefinitionRepository fieldRepository,
                              ProfileChangeRequestRepository changeRequestRepository,
                              EmployeeEmergencyContactRepository contactRepository,
                              EmployeeDependentRepository dependentRepository,
                              EmployeeEducationRepository educationRepository,
                              EmployeeExperienceRepository experienceRepository,
                              EmployeeSkillRepository skillRepository,
                              EmployeeCertificationRepository certificationRepository,
                              AuditService auditService,
                              ObjectMapper objectMapper) {
        this.employeeRepository = employeeRepository;
        this.fieldRepository = fieldRepository;
        this.changeRequestRepository = changeRequestRepository;
        this.contactRepository = contactRepository;
        this.dependentRepository = dependentRepository;
        this.educationRepository = educationRepository;
        this.experienceRepository = experienceRepository;
        this.skillRepository = skillRepository;
        this.certificationRepository = certificationRepository;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> NotFoundException.of("Employee", employeeId));

        UUID tenantId = employee.getTenantId();

        List<ProfileFieldDefinition> definitions = getOrInitDefinitions(tenantId);

        ProfileResponse response = ProfileResponse.builder()
                .personal(ProfileResponse.PersonalInfo.builder()
                        .firstName(employee.getFirstName())
                        .lastName(employee.getLastName())
                        .preferredName(employee.getPreferredName())
                        .email(employee.getEmail())
                        .phoneNumber(employee.getPhoneNumber())
                        .dateOfBirth(employee.getDateOfBirth())
                        .bankAccountNumber(employee.getBankAccountNumber())
                        .taxId(employee.getTaxId())
                        .build())
                .emergencyContacts(contactRepository.findByEmployeeId(employeeId).stream()
                        .map(c -> new ProfileResponse.EmergencyContactDto(c.getId(), c.getName(), c.getRelationship(), c.getPhone()))
                        .collect(Collectors.toList()))
                .dependents(dependentRepository.findByEmployeeId(employeeId).stream()
                        .map(d -> new ProfileResponse.DependentDto(d.getId(), d.getName(), d.getRelationship(), d.getDateOfBirth()))
                        .collect(Collectors.toList()))
                .education(educationRepository.findByEmployeeId(employeeId).stream()
                        .map(e -> new ProfileResponse.EducationDto(e.getId(), e.getInstitution(), e.getDegree(), e.getYearOfPassing()))
                        .collect(Collectors.toList()))
                .experience(experienceRepository.findByEmployeeId(employeeId).stream()
                        .map(e -> new ProfileResponse.ExperienceDto(e.getId(), e.getCompanyName(), e.getRole(), e.getStartDate(), e.getEndDate()))
                        .collect(Collectors.toList()))
                .skills(skillRepository.findByEmployeeId(employeeId).stream()
                        .map(s -> new ProfileResponse.SkillDto(s.getId(), s.getSkillName(), s.getProficiency()))
                        .collect(Collectors.toList()))
                .certifications(certificationRepository.findByEmployeeId(employeeId).stream()
                        .map(c -> new ProfileResponse.CertificationDto(c.getId(), c.getCertificationName(), c.getIssuer(), c.getExpiryDate()))
                        .collect(Collectors.toList()))
                .build();

        applyFieldFilters(response, definitions);
        return response;
    }

    @Override
    @Transactional
    public void updateProfile(UUID employeeId, Map<String, String> updates) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> NotFoundException.of("Employee", employeeId));

        UUID tenantId = employee.getTenantId();
        List<ProfileFieldDefinition> definitions = getOrInitDefinitions(tenantId);
        Map<String, ProfileFieldDefinition> defMap = definitions.stream()
                .collect(Collectors.toMap(ProfileFieldDefinition::getFieldKey, d -> d));

        for (Map.Entry<String, String> entry : updates.entrySet()) {
            String key = entry.getKey();
            ProfileFieldDefinition def = defMap.get(key);
            if (def == null) {
                throw new ConflictException("Unknown profile field: " + key);
            }
            if (!def.isEmployeeEditable()) {
                throw new AccessDeniedException("Field is not editable by employee: " + key);
            }
            if (def.isApprovalRequired()) {
                throw new ConflictException("Field " + key + " requires approval. Please submit a profile change request instead.");
            }

            applyDirectUpdate(employee, key, entry.getValue());
        }

        employeeRepository.save(employee);
        auditService.record(AuditEvent.of(AuditAction.UPDATE, "employee")
                .withEntityId(employeeId)
                .withDetail("Direct self-service profile update: " + updates.keySet()));
    }

    @Override
    @Transactional
    public ProfileChangeRequestResponse submitChangeRequest(UUID employeeId, Map<String, String> updates) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> NotFoundException.of("Employee", employeeId));

        UUID tenantId = employee.getTenantId();
        List<ProfileFieldDefinition> definitions = getOrInitDefinitions(tenantId);
        Map<String, ProfileFieldDefinition> defMap = definitions.stream()
                .collect(Collectors.toMap(ProfileFieldDefinition::getFieldKey, d -> d));

        // Validate all fields are editable
        for (String key : updates.keySet()) {
            ProfileFieldDefinition def = defMap.get(key);
            if (def == null) {
                throw new ConflictException("Unknown profile field: " + key);
            }
            if (!def.isEmployeeEditable()) {
                throw new AccessDeniedException("Field is not editable by employee: " + key);
            }
        }

        UUID requesterId = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElse(UUID.fromString("00000000-0000-0000-0000-000000000000"));

        String json;
        try {
            json = objectMapper.writeValueAsString(updates);
        } catch (Exception e) {
            throw new ConflictException("Failed to serialize updates: " + e.getMessage());
        }

        ProfileChangeRequest request = ProfileChangeRequest.builder()
                .employeeId(employeeId)
                .status("PENDING")
                .requestedBy(requesterId)
                .requestedAt(Instant.now())
                .changeJson(json)
                .build();

        ProfileChangeRequest saved = changeRequestRepository.save(request);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfileChangeRequestResponse> getPendingRequests() {
        return changeRequestRepository.findAll().stream()
                .filter(r -> "PENDING".equals(r.getStatus()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileChangeRequestResponse getRequest(UUID requestId) {
        ProfileChangeRequest req = changeRequestRepository.findById(requestId)
                .orElseThrow(() -> NotFoundException.of("ProfileChangeRequest", requestId));
        return toResponse(req);
    }

    @Override
    @Transactional
    public void approveRequest(UUID requestId) {
        ProfileChangeRequest request = changeRequestRepository.findById(requestId)
                .orElseThrow(() -> NotFoundException.of("ProfileChangeRequest", requestId));

        if (!"PENDING".equals(request.getStatus())) {
            throw new ConflictException("Change request is not in PENDING state");
        }

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> NotFoundException.of("Employee", request.getEmployeeId()));

        Map<String, String> updates;
        try {
            updates = objectMapper.readValue(request.getChangeJson(), new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            throw new ConflictException("Failed to deserialize updates: " + e.getMessage());
        }

        for (Map.Entry<String, String> entry : updates.entrySet()) {
            applyDirectUpdate(employee, entry.getKey(), entry.getValue());
        }

        employeeRepository.save(employee);

        UUID approverId = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElse(UUID.fromString("88888888-8888-8888-8888-888888888888"));

        request.setStatus("APPROVED");
        request.setApprovedBy(approverId);
        request.setApprovedAt(Instant.now());
        changeRequestRepository.save(request);

        auditService.record(AuditEvent.of(AuditAction.UPDATE, "employee")
                .withEntityId(employee.getId())
                .withDetail("Profile change request " + requestId + " approved by HR. Updates: " + updates.keySet()));
    }

    @Override
    @Transactional
    public void rejectRequest(UUID requestId) {
        ProfileChangeRequest request = changeRequestRepository.findById(requestId)
                .orElseThrow(() -> NotFoundException.of("ProfileChangeRequest", requestId));

        if (!"PENDING".equals(request.getStatus())) {
            throw new ConflictException("Change request is not in PENDING state");
        }

        UUID approverId = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElse(UUID.fromString("88888888-8888-8888-8888-888888888888"));

        request.setStatus("REJECTED");
        request.setApprovedBy(approverId);
        request.setApprovedAt(Instant.now());
        changeRequestRepository.save(request);
    }

    @Override
    @Transactional
    public void cancelRequest(UUID requestId) {
        ProfileChangeRequest request = changeRequestRepository.findById(requestId)
                .orElseThrow(() -> NotFoundException.of("ProfileChangeRequest", requestId));

        if (!"PENDING".equals(request.getStatus())) {
            throw new ConflictException("Change request is not in PENDING state");
        }

        request.setStatus("CANCELLED");
        changeRequestRepository.save(request);
    }

    private List<ProfileFieldDefinition> getOrInitDefinitions(UUID tenantId) {
        List<ProfileFieldDefinition> list = fieldRepository.findAll().stream()
                .filter(d -> tenantId.equals(d.getTenantId()))
                .collect(Collectors.toList());

        if (list.isEmpty()) {
            // Seed default values
            String[][] defaults = {
                    {"personal.firstName", "First Name", "true", "false", "true", "true", "false", "NONE"},
                    {"personal.lastName", "Last Name", "true", "false", "true", "true", "false", "NONE"},
                    {"personal.preferredName", "Preferred Name", "true", "true", "true", "true", "false", "NONE"},
                    {"personal.email", "Work Email", "true", "true", "true", "true", "false", "NONE"},
                    {"personal.phoneNumber", "Phone Number", "true", "true", "true", "true", "false", "NONE"},
                    {"personal.dateOfBirth", "Date of Birth", "true", "false", "true", "true", "false", "NONE"},
                    {"personal.bankAccountNumber", "Bank Account Number", "true", "true", "false", "true", "true", "LAST_4"},
                    {"personal.taxId", "Tax Identifier", "true", "true", "false", "true", "true", "LAST_4"}
            };

            for (String[] def : defaults) {
                ProfileFieldDefinition p = ProfileFieldDefinition.builder()
                        .tenantId(tenantId)
                        .fieldKey(def[0])
                        .displayName(def[1])
                        .employeeVisible(Boolean.parseBoolean(def[2]))
                        .employeeEditable(Boolean.parseBoolean(def[3]))
                        .managerVisible(Boolean.parseBoolean(def[4]))
                        .hrVisible(Boolean.parseBoolean(def[5]))
                        .approvalRequired(Boolean.parseBoolean(def[6]))
                        .maskingPolicy(def[7])
                        .build();
                fieldRepository.save(p);
                list.add(p);
            }
        }
        return list;
    }

    private void applyFieldFilters(ProfileResponse profile, List<ProfileFieldDefinition> definitions) {
        for (ProfileFieldDefinition def : definitions) {
            String key = def.getFieldKey();
            boolean visible = def.isEmployeeVisible();
            String mask = def.getMaskingPolicy();

            if (!visible) {
                clearField(profile, key);
            } else if ("LAST_4".equals(mask)) {
                maskField(profile, key);
            }
        }
    }

    private void clearField(ProfileResponse profile, String key) {
        if (profile.getPersonal() == null) return;
        switch (key) {
            case "personal.firstName" -> profile.getPersonal().setFirstName(null);
            case "personal.lastName" -> profile.getPersonal().setLastName(null);
            case "personal.preferredName" -> profile.getPersonal().setPreferredName(null);
            case "personal.email" -> profile.getPersonal().setEmail(null);
            case "personal.phoneNumber" -> profile.getPersonal().setPhoneNumber(null);
            case "personal.dateOfBirth" -> profile.getPersonal().setDateOfBirth(null);
            case "personal.bankAccountNumber" -> profile.getPersonal().setBankAccountNumber(null);
            case "personal.taxId" -> profile.getPersonal().setTaxId(null);
        }
    }

    private void maskField(ProfileResponse profile, String key) {
        if (profile.getPersonal() == null) return;
        switch (key) {
            case "personal.bankAccountNumber" -> {
                String val = profile.getPersonal().getBankAccountNumber();
                if (val != null && val.length() > 4) {
                    profile.getPersonal().setBankAccountNumber("******" + val.substring(val.length() - 4));
                }
            }
            case "personal.taxId" -> {
                String val = profile.getPersonal().getTaxId();
                if (val != null && val.length() > 4) {
                    profile.getPersonal().setTaxId("******" + val.substring(val.length() - 4));
                }
            }
        }
    }

    private void applyDirectUpdate(Employee employee, String key, String value) {
        switch (key) {
            case "personal.preferredName" -> employee.setPreferredName(value);
            case "personal.email" -> employee.setEmail(value);
            case "personal.phoneNumber" -> employee.setPhoneNumber(value);
            case "personal.bankAccountNumber" -> employee.setBankAccountNumber(value);
            case "personal.taxId" -> employee.setTaxId(value);
        }
    }

    private ProfileChangeRequestResponse toResponse(ProfileChangeRequest req) {
        return ProfileChangeRequestResponse.builder()
                .id(req.getId())
                .employeeId(req.getEmployeeId())
                .status(req.getStatus())
                .requestedBy(req.getRequestedBy())
                .requestedAt(req.getRequestedAt())
                .approvedBy(req.getApprovedBy())
                .approvedAt(req.getApprovedAt())
                .changeJson(req.getChangeJson())
                .build();
    }
}

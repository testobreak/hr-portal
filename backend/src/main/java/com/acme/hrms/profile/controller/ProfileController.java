package com.acme.hrms.profile.controller;

import com.acme.hrms.common.security.CurrentUser;
import com.acme.hrms.profile.dto.ProfileChangeRequestResponse;
import com.acme.hrms.profile.dto.ProfileResponse;
import com.acme.hrms.profile.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.acme.hrms.common.error.NotFoundException;
import com.acme.hrms.employee.repository.EmployeeRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ProfileController {

    private final ProfileService profileService;
    private final EmployeeRepository employeeRepository;

    public ProfileController(ProfileService profileService, EmployeeRepository employeeRepository) {
        this.profileService = profileService;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/me/profile")
    public ResponseEntity<ProfileResponse> getMyProfile() {
        UUID subjectUuid = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElseThrow(() -> new IllegalStateException("User not found in security context"));
        UUID myId = subjectUuid;
        if (!employeeRepository.existsById(myId)) {
            throw NotFoundException.of("Employee", myId);
        }
        return ResponseEntity.ok(profileService.getProfile(myId));
    }

    @PatchMapping("/me/profile")
    public ResponseEntity<Void> updateMyProfile(@RequestBody Map<String, String> updates) {
        UUID subjectUuid = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElseThrow(() -> new IllegalStateException("User not found in security context"));
        UUID myId = subjectUuid;
        if (!employeeRepository.existsById(myId)) {
            throw NotFoundException.of("Employee", myId);
        }
        profileService.updateProfile(myId, updates);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/profile-change-requests")
    public ResponseEntity<ProfileChangeRequestResponse> submitChangeRequest(@RequestBody Map<String, String> updates) {
        UUID subjectUuid = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElseThrow(() -> new IllegalStateException("User not found in security context"));
        UUID myId = subjectUuid;
        if (!employeeRepository.existsById(myId)) {
            throw NotFoundException.of("Employee", myId);
        }
        ProfileChangeRequestResponse response = profileService.submitChangeRequest(myId, updates);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me/profile-change-requests")
    public ResponseEntity<List<ProfileChangeRequestResponse>> getPendingRequestsForHR() {
        return ResponseEntity.ok(profileService.getPendingRequests());
    }

    @GetMapping("/profile-change-requests/{requestId}")
    public ResponseEntity<ProfileChangeRequestResponse> getChangeRequest(@PathVariable UUID requestId) {
        return ResponseEntity.ok(profileService.getRequest(requestId));
    }

    @PostMapping("/profile-change-requests/{requestId}/approve")
    public ResponseEntity<Void> approveChangeRequest(@PathVariable UUID requestId) {
        profileService.approveRequest(requestId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/profile-change-requests/{requestId}/reject")
    public ResponseEntity<Void> rejectChangeRequest(@PathVariable UUID requestId) {
        profileService.rejectRequest(requestId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/profile-change-requests/{requestId}/cancel")
    public ResponseEntity<Void> cancelChangeRequest(@PathVariable UUID requestId) {
        profileService.cancelRequest(requestId);
        return ResponseEntity.ok().build();
    }
}

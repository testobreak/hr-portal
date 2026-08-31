package com.acme.hrms.profile.service;

import com.acme.hrms.profile.dto.ProfileChangeRequestResponse;
import com.acme.hrms.profile.dto.ProfileResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ProfileService {
    ProfileResponse getProfile(UUID employeeId);
    void updateProfile(UUID employeeId, Map<String, String> updates);
    ProfileChangeRequestResponse submitChangeRequest(UUID employeeId, Map<String, String> updates);
    List<ProfileChangeRequestResponse> getPendingRequests();
    ProfileChangeRequestResponse getRequest(UUID requestId);
    void approveRequest(UUID requestId);
    void rejectRequest(UUID requestId);
    void cancelRequest(UUID requestId);
}

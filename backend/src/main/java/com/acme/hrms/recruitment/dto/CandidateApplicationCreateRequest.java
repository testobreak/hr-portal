package com.acme.hrms.recruitment.dto;

public record CandidateApplicationCreateRequest(
        String firstName,
        String lastName,
        String email,
        String phone,
        String resumeStorageKey,
        String skills,
        String profileSummary,
        String coverLetter,
        String source
) {
}

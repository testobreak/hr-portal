package com.acme.hrms.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponse {
    private PersonalInfo personal;
    private List<EmergencyContactDto> emergencyContacts;
    private List<DependentDto> dependents;
    private List<EducationDto> education;
    private List<ExperienceDto> experience;
    private List<SkillDto> skills;
    private List<CertificationDto> certifications;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PersonalInfo {
        private String firstName;
        private String lastName;
        private String preferredName;
        private String email;
        private String phoneNumber;
        private LocalDate dateOfBirth;
        private String bankAccountNumber;
        private String taxId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmergencyContactDto {
        private UUID id;
        private String name;
        private String relationship;
        private String phone;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DependentDto {
        private UUID id;
        private String name;
        private String relationship;
        private LocalDate dateOfBirth;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EducationDto {
        private UUID id;
        private String institution;
        private String degree;
        private Integer yearOfPassing;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExperienceDto {
        private UUID id;
        private String companyName;
        private String role;
        private LocalDate startDate;
        private LocalDate endDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SkillDto {
        private UUID id;
        private String skillName;
        private String proficiency;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CertificationDto {
        private UUID id;
        private String certificationName;
        private String issuer;
        private LocalDate expiryDate;
    }
}

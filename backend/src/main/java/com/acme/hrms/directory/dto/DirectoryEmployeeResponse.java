package com.acme.hrms.directory.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DirectoryEmployeeResponse {
      private UUID id;
      private String fullName;
      private String designationTitle;
      private String departmentName;
      private String locationName;
      private String email;
      private String phoneNumber;
      private String managerName;
}

package com.acme.hrms.manager.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ManagerHierarchyId implements Serializable {
    private UUID tenantId;
    private UUID managerId;
    private UUID subordinateId;
    private LocalDate effectiveFrom;
}

package com.acme.hrms.profile.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProfileFieldDefinitionId implements Serializable {
    private UUID tenantId;
    private String fieldKey;
}

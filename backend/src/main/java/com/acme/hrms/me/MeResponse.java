package com.acme.hrms.me;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Identity and roles of the currently authenticated caller, "
        + "as parsed from the bearer JWT.")
public record MeResponse(
        @Schema(description = "Keycloak subject UUID (sub claim).") UUID subjectUuid,
        @Schema(description = "preferred_username claim, if present.") String username,
        @Schema(description = "email claim, if present.") String email,
        @Schema(description = "Realm roles from realm_access.roles, in alphabetical order.")
        List<String> roles,
        @Schema(description = "X-Request-Id of this request, for log correlation.")
        String requestId
) {
}

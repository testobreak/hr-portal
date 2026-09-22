package com.acme.hrms.common.error;

import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Clean 200 OK response payload returned on GET requests when requested data does not exist,
 * preventing 500/404 failures while clearly communicating that data was not found.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DataNotFoundResponse(
        int status,
        String code,
        String message,
        String detail,
        Object data,
        String instance,
        Instant timestamp
) {
    public static DataNotFoundResponse of(String detail, String instance) {
        return new DataNotFoundResponse(
                200,
                "NOT_FOUND",
                "Data does not exist",
                detail != null ? detail : "Requested data does not exist",
                null,
                instance,
                Instant.now()
        );
    }
}

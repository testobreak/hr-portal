package com.acme.hrms.common.error;

/**
 * Single field-level validation failure. Used inside {@link ApiError#fieldErrors()}.
 *
 * <p>{@code code} is a stable machine string (e.g. {@code PAST_REQUIRED},
 * {@code NOT_BLANK}, {@code SIZE}); UI maps it to a localised message.
 */
public record FieldError(String field, String code, String message) {
}

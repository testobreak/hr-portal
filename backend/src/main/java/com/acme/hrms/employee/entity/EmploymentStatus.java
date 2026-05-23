package com.acme.hrms.employee.entity;

/**
 * Lifecycle state of an employee. Mirrored by the DB CHECK constraint
 * {@code ck_employee_status} on the {@code employee} table — keep in sync
 * with V2 (and any future correction migration).
 *
 * <ul>
 *   <li>{@link #ACTIVE} — currently employed and working.</li>
 *   <li>{@link #ON_LEAVE} — long leave (parental, sabbatical, medical).
 *       Still on the roster.</li>
 *   <li>{@link #TERMINATED} — separation initiated by the company.</li>
 *   <li>{@link #RESIGNED} — separation initiated by the employee.</li>
 *   <li>{@link #ABSCONDED} — left without notice; followed up by HR.</li>
 * </ul>
 */
public enum EmploymentStatus {
    ACTIVE,
    ON_LEAVE,
    TERMINATED,
    RESIGNED,
    ABSCONDED
}

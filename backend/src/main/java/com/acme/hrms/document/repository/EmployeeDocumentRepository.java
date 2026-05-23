package com.acme.hrms.document.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.acme.hrms.document.entity.EmployeeDocument;

@Repository
public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument, UUID> {

    List<EmployeeDocument> findByEmployee_IdOrderByCreatedAtDesc(UUID employeeId);

    /**
     * Removes every row, including soft-deleted ones. {@link JpaRepository#deleteAll()}
     * only loads live rows (see {@code @SQLRestriction} on {@link EmployeeDocument}), so
     * soft-deleted documents would keep their FK to {@code employee} and break test cleanup.
     * <p><strong>Integration tests only.</strong>
     */
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM employee_document", nativeQuery = true)
    void deleteAllRowsForTests();
}

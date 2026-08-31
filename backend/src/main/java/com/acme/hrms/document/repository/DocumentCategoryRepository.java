package com.acme.hrms.document.repository;

import com.acme.hrms.document.entity.DocumentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentCategoryRepository extends JpaRepository<DocumentCategory, UUID> {
    Optional<DocumentCategory> findByCodeIgnoreCase(String code);
}

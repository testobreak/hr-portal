package com.acme.hrms.workflow.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.acme.hrms.workflow.entity.ApprovalRequest;
import com.acme.hrms.workflow.entity.ApprovalStatus;

@Repository
public interface ApprovalRequestRepository
        extends JpaRepository<ApprovalRequest, UUID>, JpaSpecificationExecutor<ApprovalRequest> {

    List<ApprovalRequest> findAllByStatus(ApprovalStatus status);
}

package com.acme.hrms.workflow.controller;

import com.acme.hrms.common.security.CurrentUser;
import com.acme.hrms.workflow.entity.WorkflowDefinition;
import com.acme.hrms.workflow.repository.WorkflowDefinitionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/workflows")
public class WorkflowAdminController {

    private final WorkflowDefinitionRepository repository;

    public WorkflowAdminController(WorkflowDefinitionRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<WorkflowDefinition> create(
            @RequestParam(name = "workflowType") String workflowType,
            @RequestParam(name = "name") String name,
            @RequestBody String rulesJson) {
        UUID tenantId = CurrentUser.fromSecurityContext()
                .map(CurrentUser::tenantId)
                .orElse(UUID.fromString("00000000-0000-0000-0000-000000000000"));

        WorkflowDefinition def = WorkflowDefinition.builder()
                .workflowType(workflowType)
                .name(name)
                .rulesJson(rulesJson)
                .status("PUBLISHED")
                .build();
        def.setTenantId(tenantId);

        WorkflowDefinition saved = repository.save(def);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<WorkflowDefinition>> list(@RequestParam(name = "workflowType", required = false) String workflowType) {
        UUID tenantId = CurrentUser.fromSecurityContext()
                .map(CurrentUser::tenantId)
                .orElse(UUID.fromString("00000000-0000-0000-0000-000000000000"));

        List<WorkflowDefinition> list = repository.findAll().stream()
                .filter(w -> tenantId.equals(w.getTenantId()))
                .collect(Collectors.toList());

        if (workflowType != null) {
            list = list.stream()
                    .filter(w -> w.getWorkflowType().equalsIgnoreCase(workflowType))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(list);
    }
}

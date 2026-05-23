package com.acme.hrms.salary.service;

import java.util.List;
import java.util.UUID;

import com.acme.hrms.common.security.CurrentUser;
import com.acme.hrms.salary.dto.SalaryCreateRequest;
import com.acme.hrms.salary.dto.SalaryResponse;

public interface SalaryService {

    List<SalaryResponse> listForSelf(CurrentUser user);

    List<SalaryResponse> listForEmployee(CurrentUser user, UUID employeeId);

    SalaryResponse create(CurrentUser user, SalaryCreateRequest request);
}

package com.reliaquest.api.controller;

import com.reliaquest.api.dto.CreateEmployeeRequest;
import com.reliaquest.api.dto.Employee;
import com.reliaquest.api.service.EmployeeService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.net.URI;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
@Validated
public class EmployeeController implements IEmployeeController<Employee, CreateEmployeeRequest> {

    private final EmployeeService employeeService;
    private final Validator validator;

    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(@PathVariable String searchString) {

        return ResponseEntity.ok(employeeService.getEmployeesByNameSearch(searchString));
    }

    @Override
    public ResponseEntity<Employee> getEmployeeById(@PathVariable String id) {
        return ResponseEntity.ok(employeeService.getByEmployeeId(id));
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        return ResponseEntity.ok(employeeService.getHighestSalaryOfEmployees());
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        return ResponseEntity.ok(employeeService.getTopTenHighestEarningEmployeeNames());
    }

    @Override
    public ResponseEntity<Employee> createEmployee(@RequestBody CreateEmployeeRequest createEmployeeRequestBody) {

        // Manual validation since overridden methods cannot strengthen parameter constraints
        Set<ConstraintViolation<CreateEmployeeRequest>> violations = validator.validate(createEmployeeRequestBody);
        if (!CollectionUtils.isEmpty(violations)) {
            String message = violations.stream()
                    .findFirst()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .orElse("Validation failed");

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }

        Employee createdEmployee = employeeService.createEmployee(createEmployeeRequestBody);

        return ResponseEntity.created(URI.create("/api/employee/" + createdEmployee.getId()))
                .body(createdEmployee);
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(@PathVariable String id) {
        return ResponseEntity.ok(employeeService.deleteEmployeeById(id));
    }
}

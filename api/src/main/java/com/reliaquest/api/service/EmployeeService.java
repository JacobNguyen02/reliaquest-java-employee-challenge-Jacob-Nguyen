package com.reliaquest.api.service;

import com.reliaquest.api.client.EmployeeClient;
import com.reliaquest.api.client.dto.EmployeeDTO;
import com.reliaquest.api.dto.Employee;
import com.reliaquest.api.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeClient employeeClient;
    private final EmployeeMapper employeeMapper;

    public List<Employee> getAllEmployees() {
        List<EmployeeDTO> allEmployeesList = employeeClient.getAllEmployees();

        return employeeMapper.toEmployees(allEmployeesList);
    }

    public List<Employee> getEmployeesByNameSearch(String searchString) {
        if (StringUtils.isBlank(searchString)) {
            log.debug("Search string is blank");
            return Collections.emptyList();
        }

        String normalizedSearchString = searchString.trim().toLowerCase();
        List<EmployeeDTO> allEmployeesDTOList = employeeClient.getAllEmployees();

        return allEmployeesDTOList.stream()
                .filter(employeeDTO -> Objects.nonNull(employeeDTO) && StringUtils.isNotBlank(employeeDTO.getEmployeeName()))
                .filter(employeeDTO -> employeeDTO.getEmployeeName().toLowerCase().contains(normalizedSearchString))
                .map(employeeMapper::toEmployee)
                .toList();
    }

    public Employee getByEmployeeId(String id) {
        if (StringUtils.isBlank(id)) {
            log.debug("Id is blank");
            return null;
        }

        EmployeeDTO employeeDTO = employeeClient.getByEmployeeId(id);
        return employeeMapper.toEmployee(employeeDTO);
    }

    public Integer getHighestSalaryOfEmployees() {
        List<EmployeeDTO> allEmployeesDTOList = employeeClient.getAllEmployees();

        return allEmployeesDTOList.stream()
                .filter(employeeDTO -> Objects.nonNull(employeeDTO) && Objects.nonNull(employeeDTO.getEmployeeSalary()))
                .map(EmployeeDTO::getEmployeeSalary)
                .max(Integer::compare)
                .orElse(null);
    }
}

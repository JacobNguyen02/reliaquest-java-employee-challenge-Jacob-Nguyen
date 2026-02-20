package com.reliaquest.api.service;

import com.reliaquest.api.client.EmployeeClient;
import com.reliaquest.api.client.dto.UpstreamDeleteEmployeeRequest;
import com.reliaquest.api.client.dto.EmployeeDTO;
import com.reliaquest.api.dto.CreateEmployeeRequest;
import com.reliaquest.api.dto.Employee;
import com.reliaquest.api.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search string must not be blank");
        }

        String normalizedSearchString = searchString.trim().toLowerCase();
        List<EmployeeDTO> allEmployeesDTOList = employeeClient.getAllEmployees();

        return allEmployeesDTOList.stream()
                .filter(employeeDTO -> Objects.nonNull(employeeDTO) &&
                        StringUtils.isNotBlank(employeeDTO.getEmployeeName()))
                .filter(employeeDTO -> employeeDTO.getEmployeeName()
                        .toLowerCase()
                        .contains(normalizedSearchString))
                .map(employeeMapper::toEmployee)
                .toList();
    }

    public Employee getByEmployeeId(String id) {
        if (StringUtils.isBlank(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee id must not be blank");
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

    public List<String> getTopTenHighestEarningEmployeeNames() {
        List<Employee> employeesList = getAllEmployees();

        return employeesList.stream()
                .filter(employee -> Objects.nonNull(employee) &&
                        Objects.nonNull(employee.getSalary()))
                .sorted(Comparator.comparing(Employee::getSalary).reversed())
                .limit(10)
                .map(Employee::getName)
                .toList();
    }

    public Employee createEmployee(CreateEmployeeRequest createEmployeeRequestBody) {
        EmployeeDTO employeeDTO = employeeClient.createEmployee(createEmployeeRequestBody);
        return employeeMapper.toEmployee(employeeDTO);
    }

    public String deleteEmployeeById(String id) {
        Employee employee = getByEmployeeId(id);

        if (Objects.isNull(employee) || StringUtils.isBlank(employee.getName())) {
            log.debug("Unable to get employee name for delete request. id={}, employee={}", id, employee);
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Unable to get employee name for id=" + id
            );
        }

        UpstreamDeleteEmployeeRequest upstreamDeleteEmployeeRequest = new UpstreamDeleteEmployeeRequest();
        upstreamDeleteEmployeeRequest.setName(employee.getName());

        boolean isDeletionSuccessful = employeeClient.deleteEmployeeByName(upstreamDeleteEmployeeRequest);

        if (!isDeletionSuccessful) {
            log.debug("Upstream delete request returned false. id={}, name={}", id, employee.getName());
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Upstream delete request returned false for name: " + employee.getName()
            );
        }

        return employee.getName();
    }
}

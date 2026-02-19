package com.reliaquest.api.service;

import com.reliaquest.api.client.EmployeeClient;
import com.reliaquest.api.client.dto.EmployeeDTO;
import com.reliaquest.api.dto.Employee;
import com.reliaquest.api.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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

}

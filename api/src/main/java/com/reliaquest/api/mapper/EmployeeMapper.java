package com.reliaquest.api.mapper;

import com.reliaquest.api.client.dto.EmployeeDTO;
import com.reliaquest.api.dto.Employee;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public Employee toEmployee(EmployeeDTO employeeDTO) {
        if (Objects.isNull(employeeDTO)) {
            return null;
        }

        return new Employee(
                employeeDTO.getId(),
                employeeDTO.getEmployeeName(),
                employeeDTO.getEmployeeSalary(),
                employeeDTO.getEmployeeAge(),
                employeeDTO.getEmployeeTitle(),
                employeeDTO.getEmployeeEmail());
    }

    public List<Employee> toEmployees(List<EmployeeDTO> employeeDTOList) {
        if (Objects.isNull(employeeDTOList) || employeeDTOList.isEmpty()) {
            return List.of();
        }

        return employeeDTOList.stream().map(this::toEmployee).toList();
    }
}

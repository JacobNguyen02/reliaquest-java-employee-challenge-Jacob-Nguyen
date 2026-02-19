package com.reliaquest.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.client.EmployeeClient;
import com.reliaquest.api.client.dto.EmployeeDTO;
import com.reliaquest.api.dto.Employee;
import com.reliaquest.api.mapper.EmployeeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    private EmployeeService employeeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private EmployeeClient employeeClient;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService(
                employeeClient,
                new EmployeeMapper()
        );
    }

    @Test
    void getAllEmployees_returnsMappedEmployees() throws IOException {

        List<EmployeeDTO> employeeDTOs =
                readJson("/response/all-employees.json",
                        new TypeReference<>() {});

        when(employeeClient.getAllEmployees())
                .thenReturn(employeeDTOs);

        List<Employee> employees = employeeService.getAllEmployees();

        assertNotNull(employees);
        assertEquals(8, employees.size());
        assertEquals("Naruto Uzumaki", employees.get(0).getName());
        assertEquals("Tony Stark", employees.get(1).getName());
        assertEquals("Taylor Swift", employees.get(2).getName());
        assertEquals("Levi Ackerman", employees.get(3).getName());
        assertEquals("Zendaya Coleman", employees.get(4).getName());
        assertEquals("Monkey D. Luffy", employees.get(5).getName());
        assertEquals("Keanu Reeves", employees.get(6).getName());
        assertEquals("Mikasa Ackerman", employees.get(7).getName());

        Employee tony = employees.get(1);
        assertEquals("9002", tony.getId());
        assertEquals(950000, tony.getSalary());
        assertEquals("Chief Innovation Engineer", tony.getTitle());
        assertEquals("tstark@starkindustries.com", tony.getEmail());
    }

    private <T> T readJson(String path, TypeReference<T> typeReference) throws IOException {

        return objectMapper.readValue(
                new ClassPathResource(path).getInputStream(),
                typeReference
        );
    }
}

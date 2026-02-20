package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.client.EmployeeClient;
import com.reliaquest.api.client.dto.EmployeeDTO;
import com.reliaquest.api.dto.CreateEmployeeRequest;
import com.reliaquest.api.dto.Employee;
import com.reliaquest.api.mapper.EmployeeMapper;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    private EmployeeService employeeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private EmployeeClient employeeClient;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService(employeeClient, new EmployeeMapper());
    }

    @Test
    void getAllEmployees_returnsMappedEmployees() throws IOException {

        List<EmployeeDTO> employeeDTOs = readJson("/response/all-employees-data.json", new TypeReference<>() {});

        when(employeeClient.getAllEmployees()).thenReturn(employeeDTOs);

        List<Employee> employees = employeeService.getAllEmployees();

        assertNotNull(employees);
        assertEquals(15, employees.size());
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
        verify(employeeClient, times(1)).getAllEmployees();
        verifyNoMoreInteractions(employeeClient);
    }

    @Test
    void getEmployeesByNameSearch_returnsPartialMatches_caseInsensitive() throws IOException {
        List<EmployeeDTO> employeeDTOs = readJson("response/all-employees-data.json", new TypeReference<>() {});

        when(employeeClient.getAllEmployees()).thenReturn(employeeDTOs);

        List<Employee> result = employeeService.getEmployeesByNameSearch("ack");

        assertNotNull(result);
        assertEquals(2, result.size());

        List<String> names = result.stream().map(Employee::getName).toList();
        assertTrue(names.contains("Levi Ackerman"));
        assertTrue(names.contains("Mikasa Ackerman"));

        verify(employeeClient, times(1)).getAllEmployees();
        verifyNoMoreInteractions(employeeClient);
    }

    @Test
    void getEmployeeById_returnEmployeeMapping() throws IOException {
        EmployeeDTO employeeDTO = readJson("response/single-employee-9001-data.json", new TypeReference<>() {});
        String employeeId = employeeDTO.getId();

        when(employeeClient.getByEmployeeId(employeeId)).thenReturn(employeeDTO);

        Employee result = employeeService.getByEmployeeId(employeeId);

        assertNotNull(result);
        assertEquals("9001", result.getId());
        assertEquals(185000, result.getSalary());
        assertEquals("Lead Ninja Operations Specialist", result.getTitle());
        assertEquals("naruto@leafvillage.com", result.getEmail());
        verify(employeeClient, times(1)).getByEmployeeId(employeeId);
        verifyNoMoreInteractions(employeeClient);
    }

    @Test
    void getHighestSalaryOfEmployees_returnsHighestSalary() throws IOException {
        List<EmployeeDTO> employeeDTOs = readJson("response/all-employees-data.json", new TypeReference<>() {});

        when(employeeClient.getAllEmployees()).thenReturn(employeeDTOs);

        Integer highestSalary = employeeService.getHighestSalaryOfEmployees();

        assertNotNull(highestSalary);
        assertEquals(950000, highestSalary);
        verify(employeeClient, times(1)).getAllEmployees();
        verifyNoMoreInteractions(employeeClient);
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_returnsTopTenInDescendingSalaryOrder() throws IOException {
        List<EmployeeDTO> employeeDTOs =
                readJson("response/all-employees-data.json", new TypeReference<>() {});

        when(employeeClient.getAllEmployees()).thenReturn(employeeDTOs);

        List<String> result = employeeService.getTopTenHighestEarningEmployeeNames();

        assertNotNull(result);
        assertEquals(10, result.size());

        // Assert ordering (top 1 and last of top 10)
        assertEquals("Tony Stark", result.get(0));
        assertEquals("Selena Kyle", result.get(9));

        verify(employeeClient, times(1)).getAllEmployees();
        verifyNoMoreInteractions(employeeClient);
    }

    @Test
    void createEmployee_returnsMappedEmployee() throws IOException {
        CreateEmployeeRequest req = readJson("request/create-employee-good-30.json", new TypeReference<>() {});

        EmployeeDTO createdDto = readJson("response/employee-30-created-data.json", new TypeReference<>() {});

        when(employeeClient.createEmployee(any(CreateEmployeeRequest.class))).thenReturn(createdDto);

        Employee created = employeeService.createEmployee(req);

        assertNotNull(created);
        assertEquals(createdDto.getId(), created.getId());
        assertEquals("Steph Curry", created.getName());
        verify(employeeClient, times(1)).createEmployee(any(CreateEmployeeRequest.class));
        verifyNoMoreInteractions(employeeClient);
    }

    @Test
    void deleteEmployeeById_returnsDeletedName() throws IOException {
        EmployeeDTO employeeDto = readJson("response/single-employee-9001-data.json", new TypeReference<>() {});
        String id = employeeDto.getId();

        when(employeeClient.getByEmployeeId(id)).thenReturn(employeeDto);
        when(employeeClient.deleteEmployeeByName(any())).thenReturn(true);

        String deletedName = employeeService.deleteEmployeeById(id);

        assertNotNull(deletedName);
        assertEquals("Naruto Uzumaki", deletedName);

        verify(employeeClient, times(1)).getByEmployeeId(id);
        verify(employeeClient, times(1)).deleteEmployeeByName(any());
        verifyNoMoreInteractions(employeeClient);
    }

    private <T> T readJson(String path, TypeReference<T> typeReference) throws IOException {

        return objectMapper.readValue(new ClassPathResource(path).getInputStream(), typeReference);
    }
}

package com.reliaquest.api.client.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EmployeeDTO {
    private String id;
    private String employeeName;
    private Integer employeeSalary;
    private Integer employeeAge;
    private String employeeTitle;
    private String employeeEmail;
}

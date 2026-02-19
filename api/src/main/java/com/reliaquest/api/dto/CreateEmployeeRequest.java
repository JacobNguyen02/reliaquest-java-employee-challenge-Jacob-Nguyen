package com.reliaquest.api.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateEmployeeRequest {

    @NotBlank
    private String name;

    @Min(1)
    private Integer salary;

    @Min(16)
    @Max(75)
    private Integer age;

    @NotBlank
    private String title;
}

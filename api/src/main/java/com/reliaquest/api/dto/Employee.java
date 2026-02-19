package com.reliaquest.api.dto;

import lombok.Value;

@Value
public class Employee {
    String id;
    String name;
    Integer salary;
    Integer age;
    String title;
    String email;
}

package com.reliaquest.api.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties("employee-client")
public class EmployeeClientProperties {
    private String baseUri;
}

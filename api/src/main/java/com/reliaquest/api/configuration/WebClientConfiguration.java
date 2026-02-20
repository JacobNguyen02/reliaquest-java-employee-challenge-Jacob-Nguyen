package com.reliaquest.api.configuration;

import com.reliaquest.api.properties.EmployeeClientProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfiguration {

    private final EmployeeClientProperties employeeClientProperties;

    @Bean("employeeWebClient")
    public WebClient employeeWebClient() {
        return WebClient.builder()
                .baseUrl(employeeClientProperties.getBaseUri())
                .build();
    }
}

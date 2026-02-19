package com.reliaquest.api.client;

import com.reliaquest.api.client.dto.EmployeeDTO;
import com.reliaquest.api.client.dto.UpstreamApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class EmployeeClient {

    private final WebClient webClient;

    public EmployeeClient(@Qualifier("employeeWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public List<EmployeeDTO> getAllEmployees() {
        try {
            UpstreamApiResponse<List<EmployeeDTO>> response =
                    webClient.get()
                            .uri("/employee")
                            .accept(MediaType.APPLICATION_JSON)
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<UpstreamApiResponse<List<EmployeeDTO>>>() {})
                            .block();

            if (Objects.isNull(response)) {
                throw new IllegalStateException("Upstream returned empty response for getAllEmployees");
            }

            log.debug("Upstream GET /employee returned status='{}' with {} records",
                    response.getStatus(),
                    !CollectionUtils.isEmpty(response.getData()) ? response.getData().size() : 0);

            if(CollectionUtils.isEmpty(response.getData())) {
                throw new IllegalStateException("Upstream returned null data");

            }

            return response.getData();
        } catch (Exception e) {
            log.error("Error calling upstream getAllEmployees", e);
            throw e;
        }
    }

    public EmployeeDTO getByEmployeeId(String id) {
        try {
            UpstreamApiResponse<EmployeeDTO> response =
                    webClient.get()
                            .uri("/employee/{id}", id)
                            .accept(MediaType.APPLICATION_JSON)
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<UpstreamApiResponse<EmployeeDTO>>() {})
                            .block();

            if (Objects.isNull(response)) {
                throw new IllegalStateException("Upstream returned empty response for getAllEmployees");
            }

            log.debug("Upstream GET /employee/{} returned status='{}'",
                    id, response.getStatus());

            if(Objects.isNull(response.getData())) {
                throw new IllegalStateException("Upstream returned null data");

            }
            return response.getData();

        } catch (WebClientResponseException.NotFound e) {
            log.warn("Upstream returned 404 for employee id={}. Body={}", id, e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("Error calling upstream getEmployeeById", e);
            throw e;
        }
    }

}

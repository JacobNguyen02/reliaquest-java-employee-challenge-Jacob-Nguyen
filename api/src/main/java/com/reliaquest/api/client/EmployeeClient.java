package com.reliaquest.api.client;

import com.reliaquest.api.client.dto.EmployeeDTO;
import com.reliaquest.api.client.dto.UpstreamApiResponse;
import com.reliaquest.api.client.dto.UpstreamDeleteEmployeeRequest;
import com.reliaquest.api.dto.CreateEmployeeRequest;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.util.retry.Retry;

@Component
@Slf4j
public class EmployeeClient {

    private final WebClient webClient;

    public EmployeeClient(@Qualifier("employeeWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public List<EmployeeDTO> getAllEmployees() {
        try {
            UpstreamApiResponse<List<EmployeeDTO>> response = webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder.path("/employee").build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<UpstreamApiResponse<List<EmployeeDTO>>>() {})
                    .retryWhen(Retry.backoff(3, Duration.ofMillis(200))
                            .maxBackoff(Duration.ofSeconds(2))
                            .filter(ex -> ex instanceof WebClientResponseException.TooManyRequests)
                            .doBeforeRetry(retrySignal -> log.warn(
                                    "Retrying GET /employee due to 429 (attempt {} of 3). Cause={}",
                                    retrySignal.totalRetries() + 1,
                                    retrySignal.failure().toString())))
                    .block();

            if (Objects.isNull(response)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY, "Upstream returned empty response for getAllEmployees");
            }

            log.debug(
                    "Upstream GET /employee returned status='{}' with {} records",
                    response.getStatus(),
                    !CollectionUtils.isEmpty(response.getData())
                            ? response.getData().size()
                            : 0);

            if (CollectionUtils.isEmpty(response.getData())) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Upstream returned null data");
            }

            return response.getData();

        } catch (WebClientResponseException ex) {
            log.error(
                    "Upstream error during getAllEmployees. status={}, body={}",
                    ex.getStatusCode(),
                    ex.getResponseBodyAsString(),
                    ex);
            throw ex;
        } catch (WebClientRequestException ex) {
            log.error("Upstream request failed during getAllEmployees", ex);
            throw ex;
        }
    }

    public EmployeeDTO getByEmployeeId(String id) {
        try {
            UpstreamApiResponse<EmployeeDTO> response = webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder.path("/employee/{id}").build(id))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<UpstreamApiResponse<EmployeeDTO>>() {})
                    .retryWhen(Retry.backoff(3, Duration.ofMillis(200))
                            .maxBackoff(Duration.ofSeconds(2))
                            .filter(ex -> ex instanceof WebClientResponseException.TooManyRequests)
                            .doBeforeRetry(retrySignal -> log.warn(
                                    "Retrying getEmployeeById id={} due to 429 (attempt {} of 3). Cause={}",
                                    id,
                                    retrySignal.totalRetries() + 1,
                                    retrySignal.failure().toString())))
                    .block();

            if (Objects.isNull(response)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY, "Upstream returned empty response for getEmployeeById");
            }

            log.debug("Upstream GET /employee/{} returned status='{}'", id, response.getStatus());

            if (Objects.isNull(response.getData())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY, "Upstream returned null data for getEmployeeById");
            }

            return response.getData();

        } catch (WebClientResponseException ex) {
            log.error(
                    "Upstream error during getEmployeeById. id={}, status={}, body={}",
                    id,
                    ex.getStatusCode(),
                    ex.getResponseBodyAsString(),
                    ex);
            throw ex;
        } catch (WebClientRequestException ex) {
            log.error("Upstream request failed during getEmployeeById. id={}", id, ex);
            throw ex;
        }
    }

    public EmployeeDTO createEmployee(CreateEmployeeRequest createEmployeeRequest) {
        try {
            UpstreamApiResponse<EmployeeDTO> response = webClient
                    .post()
                    .uri(uriBuilder -> uriBuilder.path("/employee").build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(createEmployeeRequest)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<UpstreamApiResponse<EmployeeDTO>>() {})
                    .retryWhen(Retry.backoff(3, Duration.ofMillis(200))
                            .maxBackoff(Duration.ofSeconds(2))
                            .filter(ex -> ex instanceof WebClientResponseException.TooManyRequests)
                            .doBeforeRetry(retrySignal -> log.warn(
                                    "Retrying POST /employee due to 429 (attempt {} of 3). Cause={}",
                                    retrySignal.totalRetries() + 1,
                                    retrySignal.failure().toString())))
                    .block();

            if (Objects.isNull(response)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY, "Upstream returned empty response for createEmployee");
            }

            log.debug("Upstream POST /employee returned status='{}'", response.getStatus());

            if (Objects.isNull(response.getData())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY, "Upstream returned null data for createEmployee");
            }

            return response.getData();

        } catch (WebClientResponseException ex) {
            log.error(
                    "Upstream error during createEmployee. status={}, body={}",
                    ex.getStatusCode(),
                    ex.getResponseBodyAsString(),
                    ex);
            throw ex;
        } catch (WebClientRequestException ex) {
            log.error("Upstream request failed during createEmployee", ex);
            throw ex;
        }
    }

    public boolean deleteEmployeeByName(UpstreamDeleteEmployeeRequest upstreamDeleteEmployeeRequest) {
        try {
            UpstreamApiResponse<Boolean> response = webClient
                    .method(HttpMethod.DELETE)
                    .uri(uriBuilder -> uriBuilder.path("/employee").build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(upstreamDeleteEmployeeRequest)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<UpstreamApiResponse<Boolean>>() {})
                    .retryWhen(Retry.backoff(3, Duration.ofMillis(200))
                            .maxBackoff(Duration.ofSeconds(2))
                            .filter(ex -> ex instanceof WebClientResponseException.TooManyRequests)
                            .doBeforeRetry(retrySignal -> log.warn(
                                    "Retrying DELETE /employee due to 429 (attempt {} of 3). name={}, cause={}",
                                    retrySignal.totalRetries() + 1,
                                    upstreamDeleteEmployeeRequest.getName(),
                                    retrySignal.failure().toString())))
                    .block();

            if (Objects.isNull(response)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY, "Upstream returned empty response for deleteEmployeeByName");
            }

            log.debug(
                    "Upstream DELETE /employee returned status='{}' for name='{}'",
                    response.getStatus(),
                    upstreamDeleteEmployeeRequest.getName());

            if (Objects.isNull(response.getData())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY, "Upstream returned null data for deleteEmployeeByName");
            }

            return response.getData();

        } catch (WebClientResponseException ex) {
            log.error(
                    "Upstream error during deleteEmployeeByName. name={}, status={}, body={}",
                    upstreamDeleteEmployeeRequest.getName(),
                    ex.getStatusCode(),
                    ex.getResponseBodyAsString(),
                    ex);
            throw ex;
        } catch (WebClientRequestException ex) {
            log.error(
                    "Upstream request failed during deleteEmployeeByName. name={}",
                    upstreamDeleteEmployeeRequest.getName(),
                    ex);
            throw ex;
        }
    }
}

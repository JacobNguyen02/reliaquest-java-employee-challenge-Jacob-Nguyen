package com.reliaquest.api.exception;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
        log.error("Handled ResponseStatusException: {}", ex.getReason(), ex);

        String message = Optional.ofNullable(ex.getReason())
                .orElse(HttpStatus.valueOf(ex.getStatusCode().value()).getReasonPhrase());

        return ResponseEntity.status(ex.getStatusCode())
                .body(Map.of("status", ex.getStatusCode().value(), "message", message));
    }

    // HTTP response received, but it's a non 2xx code
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<Map<String, Object>> handleWebClientResponseException(WebClientResponseException ex) {
        log.error(
                "Handled WebClientResponseException: status={}, body={}",
                ex.getStatusCode().value(),
                ex.getResponseBodyAsString(),
                ex);

        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String message;
        if (status == HttpStatus.NOT_FOUND) {
            message = "Resource not found";
        } else if (status == HttpStatus.TOO_MANY_REQUESTS) {
            message = "Upstream rate limited requests";
        } else {
            message = Optional.of(ex.getResponseBodyAsString())
                    .filter(StringUtils::isNotBlank)
                    .orElseGet(() -> Optional.of(ex.getStatusText())
                            .filter(s -> !s.isBlank())
                            .orElse(status.getReasonPhrase()));
        }
        return ResponseEntity.status(ex.getStatusCode())
                .body(Map.of("status", ex.getStatusCode().value(), "message", message));
    }

    // No Http response received from client
    @ExceptionHandler(WebClientRequestException.class)
    public ResponseEntity<Map<String, Object>> handleWebClientRequestException(WebClientRequestException ex) {
        log.error("Handled WebClientRequestException: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("status", HttpStatus.BAD_GATEWAY.value(), "message", "Upstream service unavailable"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Throwable exceptionCause = ex;

        // Walk the exception cause chain to preserve the original HTTP status when exceptions are wrapped
        // (retry/app/clients).
        while (Objects.nonNull(exceptionCause)) {
            if (exceptionCause instanceof ResponseStatusException rse) {
                return handleResponseStatusException(rse);
            }
            if (exceptionCause instanceof WebClientResponseException wcre) {
                return handleWebClientResponseException(wcre);
            }
            if (exceptionCause instanceof WebClientRequestException wcreq) {
                return handleWebClientRequestException(wcreq);
            }
            exceptionCause = exceptionCause.getCause();
        }

        log.error("Unhandled exception", ex);

        return ResponseEntity.status(500).body(Map.of("status", 500, "message", "Internal server error"));
    }
}

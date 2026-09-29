package com.trustlayer.shared.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;

import static com.trustlayer.shared.web.CorrelationIdFilter.MDC_KEY;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String correlationId,
        List<FieldViolation> details) {

    public record FieldViolation(String field, String message) {
    }

    public static ErrorResponse of(HttpStatus status, String code, String message) {
        return of(status, code, message, List.of());
    }

    public static ErrorResponse of(HttpStatus status, String code, String message, List<FieldViolation> details) {
        return new ErrorResponse(Instant.now(), status.value(), code, message, MDC.get(MDC_KEY), details);
    }
}

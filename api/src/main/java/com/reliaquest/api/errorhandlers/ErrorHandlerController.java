package com.reliaquest.api.errorhandlers;

import static com.reliaquest.api.util.Constants.BAD_REQUEST;
import static com.reliaquest.api.util.Constants.CIRCUIT_BREAKER_OPEN;
import static com.reliaquest.api.util.Constants.HTTP_ERROR;
import static com.reliaquest.api.util.Constants.INTERNAL_SERVER_ERROR;
import static com.reliaquest.api.util.Constants.INVALID_REQUEST;
import static com.reliaquest.api.util.Constants.TOO_MANY_REQUESTS;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

@RestControllerAdvice
@Slf4j
@AllArgsConstructor
public class ErrorHandlerController {
    private final MessageSource messageSource;

    private APIError buildAPIError(String msgId, Object[] params, Map<String, String> errors, Integer status) {
        APIError apiError = new APIError();
        try {
            var message = messageSource.getMessage(msgId, params, LocaleContextHolder.getLocale());
            apiError.setMessage(message);
        } catch (NoSuchMessageException e) {
            apiError.setMessage("");
        }
        apiError.setId(msgId);
        apiError.setErrors(errors);
        apiError.setStatus(Objects.nonNull(status) ? String.valueOf(status) : "");
        return apiError;
    }

    @ExceptionHandler(APIException.class)
    public ResponseEntity<APIError> handleAPIException(APIException ex) {
        log.error("API error :", ex);
        APIError apiError = buildAPIError(
                ex.getMsgId(), ex.getParams(), null, ex.getHttpStatusCode().value());
        return ResponseEntity.status(ex.getHttpStatusCode()).body(apiError);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<APIError> handleHttpClientError(HttpClientErrorException ex) {
        log.error(
                "HTTP Client Error: Status code: {}, Response body: {}",
                ex.getStatusCode(),
                ex.getResponseBodyAsString());
        var msgId = HTTP_ERROR;
        if (ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
            msgId = TOO_MANY_REQUESTS;
        }
        APIError apiError = buildAPIError(msgId, null, null, ex.getStatusCode().value());
        return ResponseEntity.status(ex.getStatusCode()).body(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIError> handleGenericException(Exception ex) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        var msgId = INTERNAL_SERVER_ERROR;
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (ex instanceof CallNotPermittedException) {
            msgId = CIRCUIT_BREAKER_OPEN;
            status = HttpStatus.SERVICE_UNAVAILABLE;
        }
        APIError apiError = buildAPIError(msgId, null, null, status.value());
        return ResponseEntity.status(status).body(apiError);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIError> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        APIError apiError = buildAPIError(
                INVALID_REQUEST, errors.keySet().toArray(new String[0]), errors, HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<APIError> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.error("Invalid request body: {}", ex.getMessage());
        APIError apiError = buildAPIError(BAD_REQUEST, null, null, HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }
}

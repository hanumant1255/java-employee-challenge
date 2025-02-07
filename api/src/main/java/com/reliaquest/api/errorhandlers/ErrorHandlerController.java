package com.reliaquest.api.controller;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import com.reliaquest.api.errorhandlers.APIError;
import com.reliaquest.api.errorhandlers.APIException;
import com.reliaquest.api.errorhandlers.EmployeeNotFoundException;
import com.reliaquest.api.errorhandlers.EmployeeServiceException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
@AllArgsConstructor
public class ErrorHandlerController {

    private final MessageSource messageSource;

    private APIError buildAPIError(String msgId, Object[] params, String defaultMessage) {
        APIError apiError = new APIError();
        apiError.setReason(messageSource.getMessage(msgId, params, LocaleContextHolder.getLocale()));
        apiError.setId(msgId);
        return apiError;
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<APIError> handleEmployeeNotFoundException(EmployeeNotFoundException ex) {
        log.error("Employee not found: {}", ex.getMessage());
        APIError apiError = buildAPIError(ex.getMsgId(), ex.getParams(), "Employee not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    @ExceptionHandler(EmployeeServiceException.class)
    public ResponseEntity<APIError> handleEmployeeServiceException(EmployeeServiceException ex) {
        log.error("Service error: {}", ex.getMessage());
        APIError apiError = buildAPIError(ex.getMsgId(), ex.getParams(), "Service error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }

    @ExceptionHandler(APIException.class)
    public ResponseEntity<APIError> handleAPIException(APIException ex) {
        log.error("API error: {}", ex.getMessage());
        APIError apiError = buildAPIError(ex.getMsgId(), ex.getParams(), "API error");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(org.springframework.web.HttpMediaTypeException.class)
    public ResponseEntity<APIError> handleHttpMediaTypeException(org.springframework.web.HttpMediaTypeException ex) {
        log.error("Unsupported media type: {}", ex.getMessage());
        APIError apiError = buildAPIError("unsupported.media.type", null, "Unsupported media type");
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(apiError);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<String> handleHttpClientError(HttpClientErrorException ex) {
        log.error("HTTP Client Error: Status code: {}, Response body: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        String errorMessage = ex.getStatusCode() == HttpStatus.NOT_FOUND
                ? "Record not found"
                : ex.getResponseBodyAsString();

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(errorMessage);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Something went wrong: " + ex.getMessage());
    }
}
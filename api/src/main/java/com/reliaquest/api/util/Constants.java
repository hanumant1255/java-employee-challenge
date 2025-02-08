package com.reliaquest.api.util;

public class Constants {
    private Constants() {}

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_JSON = "application/json";
    public static final String API_V_1_EMPLOYEE = "/api/v1/employee";
    public static final String SLASH = "/";

    public static final String MOCK_SERVICE_API_RETRY = "mockServiceApiRetry";
    public static final String MOCK_SERVICE_API_CIRCUIT_BREAKER = "mockServiceApiCircuitBreaker";

    public static final String INTERNAL_SERVER_ERROR = "internal.server.error";
    public static final String HTTP_ERROR = "http.error";
    public static final String TOO_MANY_REQUESTS = "too.many.requests";
    public static final String INVALID_REQUEST = "invalid.request";
    public static final String BAD_REQUEST = "bad.request";
    public static final String CIRCUIT_BREAKER_OPEN = "circuit.breaker.open";
}

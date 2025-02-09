package com.reliaquest.api.repository;

import static com.reliaquest.api.util.Constants.API_V_1_EMPLOYEE;
import static com.reliaquest.api.util.Constants.APPLICATION_JSON;
import static com.reliaquest.api.util.Constants.CONTENT_TYPE;
import static com.reliaquest.api.util.Constants.SLASH;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

import com.reliaquest.api.config.AppProperties;
import com.reliaquest.api.dto.EmployeeDeleteRequest;
import com.reliaquest.api.dto.EmployeeRequest;
import com.reliaquest.api.model.EmployeeApiResponse;
import com.reliaquest.api.model.EmployeeDeleteApiResponse;
import com.reliaquest.api.model.EmployeeListApiResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@AllArgsConstructor
@Slf4j
public class MockEmployeeRestClient {

    private final RestTemplate restTemplate;
    private final AppProperties appProperties;

    private String getUrl() {
        return appProperties.getMockEmployeeService().getUrl() + API_V_1_EMPLOYEE;
    }

    private HttpHeaders createJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(CONTENT_TYPE, APPLICATION_JSON);
        return headers;
    }

    private <T> HttpEntity<?> createHttpEntity(Object requestBody) {
        return requestBody == null
                ? new HttpEntity<>(createJsonHeaders())
                : new HttpEntity<>(requestBody, createJsonHeaders());
    }

    private <T> T executeApiCall(String url, HttpMethod method, Object requestBody, Class<T> responseType) {
        log.debug("Executing API call: [{}] {}", method, url);
        try {
            ResponseEntity<T> response;

            if (method == GET) {
                response = restTemplate.getForEntity(url, responseType);
            } else if (method == POST) {
                response = restTemplate.postForEntity(url, createHttpEntity(requestBody), responseType);
            } else if (method == DELETE) {
                response = restTemplate.exchange(url, DELETE, createHttpEntity(requestBody), responseType);
            } else {
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
            }

            return handleResponse(response, url, method);

        } catch (HttpClientErrorException ex) {
            log.warn(
                    "API call failed [{} {}] - Status: {}, Response: {}",
                    method,
                    url,
                    ex.getStatusCode(),
                    ex.getResponseBodyAsString());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error during API call [{} {}]: {}", method, url, ex.getMessage(), ex);
            throw ex;
        }
    }

    private <T> T handleResponse(ResponseEntity<T> response, String url, HttpMethod method) {
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            log.trace("Successful API call [{} {}]", method, url);
            return response.getBody();
        }
        log.error("API call [{} {}] failed with status: {}", method, url, response.getStatusCode());
        throw new RuntimeException("API call failed with status: " + response.getStatusCode());
    }

    public EmployeeListApiResponse getAllEmployees() {
        return executeApiCall(getUrl(), GET, null, EmployeeListApiResponse.class);
    }

    public EmployeeApiResponse getEmployeeById(String id) {
        return executeApiCall(getUrl() + SLASH + id, GET, null, EmployeeApiResponse.class);
    }

    public EmployeeApiResponse createEmployee(EmployeeRequest employeeRequest) {
        return executeApiCall(getUrl(), POST, employeeRequest, EmployeeApiResponse.class);
    }

    public EmployeeDeleteApiResponse deleteEmployeeByName(EmployeeDeleteRequest employeeDeleteRequest) {
        return executeApiCall(getUrl(), DELETE, employeeDeleteRequest, EmployeeDeleteApiResponse.class);
    }
}

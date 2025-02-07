package com.reliaquest.api.repository;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.reliaquest.api.config.AppProperties;
import com.reliaquest.api.dto.EmployeeRequest;
import com.reliaquest.api.model.EmployeeApiResponse;
import com.reliaquest.api.model.EmployeeListApiResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.reliaquest.api.util.Constants.API_V_1_EMPLOYEE;
import static com.reliaquest.api.util.Constants.APPLICATION_JSON;
import static com.reliaquest.api.util.Constants.CONTENT_TYPE;
import static com.reliaquest.api.util.Constants.SLASH;

@Component
@AllArgsConstructor
@Slf4j
public class MockEmployeeRestClient {

    private final RestTemplate restTemplate;
    private final AppProperties appProperties;

    private String buildUrl(String endpoint) {
        return appProperties.getMockEmployeeService().getUrl() + API_V_1_EMPLOYEE + endpoint;
    }

    private HttpHeaders createJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(CONTENT_TYPE, APPLICATION_JSON);
        return headers;
    }

    private <T> T executeApiCall(String url, HttpMethod method, Object requestBody, Class<T> responseType) {
        log.debug("Calling API: {} {}", method, url);
        try {
            ResponseEntity<T> response;
            if (method == HttpMethod.GET) {
                response = restTemplate.getForEntity(url, responseType);
            } else if (method == HttpMethod.POST) {
                HttpEntity<?> entity = new HttpEntity<>(requestBody, createJsonHeaders());
                response = restTemplate.postForEntity(url, entity, responseType);
            } else if (method == HttpMethod.DELETE) {
                response = restTemplate.exchange(url, HttpMethod.DELETE, null, responseType);
            } else {
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
            }
            return handleResponse(response, url, method);

        } catch (HttpClientErrorException ex) {
            log.error(
                    "API call failed: {} {} - Status: {}, Response: {}",
                    method,
                    url,
                    ex.getStatusCode(),
                    ex.getResponseBodyAsString(),
                    ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error calling API: {} {}", method, url, ex);
            throw ex;
        }
    }

    private <T> T handleResponse(ResponseEntity<T> response, String url, HttpMethod method) {
        if (response.getStatusCode().is2xxSuccessful()) {
            log.trace("Successful API call: {} {}", method, url);
            return response.getBody();
        } else {
            log.error("API call failed: {} {} - Status: {}", method, url, response.getStatusCode());
            throw new RuntimeException(
                    "API call failed with status: " + response.getStatusCode()); // Or custom exception
        }
    }

    public EmployeeListApiResponse getAllEmployees() {
        String url = buildUrl("");
        return executeApiCall(url, HttpMethod.GET, null, EmployeeListApiResponse.class);
    }

    public EmployeeApiResponse getEmployeeById(String id) {
        String url = buildUrl(SLASH + id);
        return executeApiCall(url, HttpMethod.GET, null, EmployeeApiResponse.class);
    }

    public EmployeeApiResponse createEmployee(EmployeeRequest employeeRequest) {
        String url = buildUrl("");
        return executeApiCall(url, HttpMethod.POST, employeeRequest, EmployeeApiResponse.class);
    }

    public Boolean deleteEmployeeByName(String name) {
        String url = buildUrl(SLASH + name);
        return executeApiCall(url, HttpMethod.DELETE, null, Boolean.class);
    }
}

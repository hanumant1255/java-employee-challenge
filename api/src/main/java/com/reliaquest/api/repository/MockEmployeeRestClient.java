package com.reliaquest.api.repository;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.reliaquest.api.config.AppProperties;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeApiResponse;
import com.reliaquest.api.model.EmployeeListApiResponse;
import lombok.AllArgsConstructor;

import static com.reliaquest.api.util.Constants.API_V_1_EMPLOYEE;
import static com.reliaquest.api.util.Constants.APPLICATION_JSON;
import static com.reliaquest.api.util.Constants.CONTENT_TYPE;
import static com.reliaquest.api.util.Constants.SLASH;


//TODO handle rate limiting
@Component
@AllArgsConstructor
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

    public EmployeeListApiResponse getAllEmployees() {
        String url = buildUrl("");
        ResponseEntity<EmployeeListApiResponse> response =
                restTemplate.getForEntity(url, EmployeeListApiResponse.class);
        return handleResponse(response);
    }

    public EmployeeApiResponse getEmployeeById(String id) {
        String url = buildUrl(SLASH + id);
        ResponseEntity<EmployeeApiResponse> response = restTemplate.getForEntity(url, EmployeeApiResponse.class);
        return handleResponse(response);
    }

    public EmployeeApiResponse createEmployee(Employee employee) {
        String url = buildUrl("");
        HttpEntity<Employee> entity = new HttpEntity<>(employee, createJsonHeaders());
        ResponseEntity<EmployeeApiResponse> response =
                restTemplate.postForEntity(url, entity, EmployeeApiResponse.class);
        return handleResponse(response);
    }

    public Boolean deleteEmployeeByName(String name) {
        String url = buildUrl(SLASH + name);
        ResponseEntity<Boolean> response =
                restTemplate.exchange(url, HttpMethod.DELETE, null, Boolean.class);
        return handleResponse(response);
    }

    private <T> T handleResponse(ResponseEntity<T> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new RuntimeException("API call failed with status: " + response.getStatusCode());
        }
    }
}

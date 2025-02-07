package com.reliaquest.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.reliaquest.api.config.AppProperties;
import com.reliaquest.api.dto.EmployeeRequest;
import com.reliaquest.api.model.EmployeeApiResponse;
import com.reliaquest.api.model.EmployeeListApiResponse;
import com.reliaquest.api.repository.MockEmployeeRestClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MockEmployeeRestClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AppProperties appProperties;

    @Mock
    private AppProperties.MockEmployeeService mockEmployeeService;

    @InjectMocks
    private MockEmployeeRestClient mockEmployeeRestClient;

    @BeforeEach
    void setUp() {
        when(appProperties.getMockEmployeeService()).thenReturn(mockEmployeeService);
        when(mockEmployeeService.getUrl()).thenReturn("http://mock-service");
    }

    @Test
    void testGetAllEmployees_Success() {
        EmployeeListApiResponse mockResponse = new EmployeeListApiResponse();
        when(restTemplate.getForEntity(eq("http://mock-service/api/v1/employee"), eq(EmployeeListApiResponse.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        EmployeeListApiResponse response = mockEmployeeRestClient.getAllEmployees();

        assertNotNull(response);
        verify(restTemplate, times(1)).getForEntity(any(String.class), eq(EmployeeListApiResponse.class));
    }

    @Test
    void testGetEmployeeById_Success() {
        EmployeeApiResponse mockResponse = new EmployeeApiResponse();
        when(restTemplate.getForEntity(eq("http://mock-service/api/v1/employee/123"), eq(EmployeeApiResponse.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        EmployeeApiResponse response = mockEmployeeRestClient.getEmployeeById("123");

        assertNotNull(response);
        verify(restTemplate, times(1)).getForEntity(any(String.class), eq(EmployeeApiResponse.class));
    }

    @Test
    void testCreateEmployee_Success() {
        EmployeeRequest request = new EmployeeRequest();
        EmployeeApiResponse mockResponse = new EmployeeApiResponse();
        when(restTemplate.postForEntity(eq("http://mock-service/api/v1/employee"), any(), eq(EmployeeApiResponse.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.CREATED));

        EmployeeApiResponse response = mockEmployeeRestClient.createEmployee(request);

        assertNotNull(response);
        verify(restTemplate, times(1)).postForEntity(any(String.class), any(), eq(EmployeeApiResponse.class));
    }

    @Test
    void testDeleteEmployeeByName_Success() {
        when(restTemplate.exchange(eq("http://mock-service/api/v1/employee/JohnDoe"), eq(HttpMethod.DELETE), eq(null), eq(Boolean.class)))
                .thenReturn(new ResponseEntity<>(true, HttpStatus.OK));

        Boolean response = mockEmployeeRestClient.deleteEmployeeByName("JohnDoe");

        assertTrue(response);
        verify(restTemplate, times(1)).exchange(any(String.class), eq(HttpMethod.DELETE), eq(null), eq(Boolean.class));
    }

    @Test
    void testGetEmployeeById_NotFound() {
        when(restTemplate.getForEntity(eq("http://mock-service/api/v1/employee/999"), eq(EmployeeApiResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThrows(HttpClientErrorException.class, () -> mockEmployeeRestClient.getEmployeeById("999"));
        verify(restTemplate, times(1)).getForEntity(any(String.class), eq(EmployeeApiResponse.class));
    }
}

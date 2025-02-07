package com.reliaquest.api;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.reliaquest.api.controller.EmployeeController;
import com.reliaquest.api.dto.EmployeeDTO;
import com.reliaquest.api.dto.EmployeeRequest;
import com.reliaquest.api.service.EmployeeService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    @Mock
    private EmployeeService<EmployeeDTO, EmployeeRequest> employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    private EmployeeDTO mockEmployee;
    private EmployeeRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockEmployee = new EmployeeDTO();
        mockEmployee.setId(UUID.randomUUID());
        mockEmployee.setEmployeeName("Hanumant Shinde");

        mockRequest = new EmployeeRequest();
        mockRequest.setName("Hanumant Shinde");
    }

    @Test
    void testGetAllEmployees() {
        when(employeeService.getAllEmployees()).thenReturn(List.of(mockEmployee));

        ResponseEntity<List<EmployeeDTO>> response = employeeController.getAllEmployees();

        assertEquals(1, Objects.requireNonNull(response.getBody()).size());
        assertEquals("Hanumant Shinde", response.getBody().get(0).getEmployeeName());
        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    void testGetEmployeesByNameSearch() {
        when(employeeService.getEmployeesByNameSearch("John"))
                .thenReturn(List.of(mockEmployee));

        ResponseEntity<List<EmployeeDTO>> response = employeeController.getEmployeesByNameSearch("John");

        assertEquals(1, Objects.requireNonNull(response.getBody()).size());
        assertEquals("Hanumant Shinde", response.getBody().get(0).getEmployeeName());
        verify(employeeService, times(1)).getEmployeesByNameSearch("John");
    }

    @Test
    void testGetEmployeeById() {
        when(employeeService.getEmployeeById("123")).thenReturn(mockEmployee);

        ResponseEntity<EmployeeDTO> response = employeeController.getEmployeeById("123");

        assertNotNull(response.getBody());
        assertEquals("Hanumant Shinde", response.getBody().getEmployeeName());
        verify(employeeService, times(1)).getEmployeeById("123");
    }

    @Test
    void testGetHighestSalaryOfEmployees() {
        when(employeeService.getHighestSalaryOfEmployees()).thenReturn(100000);

        ResponseEntity<Integer> response = employeeController.getHighestSalaryOfEmployees();

        assertEquals(100000, response.getBody());
        verify(employeeService, times(1)).getHighestSalaryOfEmployees();
    }

    @Test
    void testGetTopTenHighestEarningEmployeeNames() {
        List<String> mockNames = List.of("Hanumant Shinde", "Jane Doe");
        when(employeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(mockNames);

        ResponseEntity<List<String>> response = employeeController.getTopTenHighestEarningEmployeeNames();

        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        assertEquals("Hanumant Shinde", response.getBody().get(0));
        verify(employeeService, times(1)).getTopTenHighestEarningEmployeeNames();
    }

    @Test
    void testCreateEmployee() {
        when(employeeService.createEmployee(mockRequest)).thenReturn(mockEmployee);

        ResponseEntity<EmployeeDTO> response = employeeController.createEmployee(mockRequest);

        assertNotNull(response.getBody());
        assertEquals("Hanumant Shinde", response.getBody().getEmployeeName());
        verify(employeeService, times(1)).createEmployee(mockRequest);
    }

    @Test
    void testDeleteEmployeeById() {
        when(employeeService.deleteEmployeeById("123")).thenReturn("Hanumant Shinde");

        ResponseEntity<String> response = employeeController.deleteEmployeeById("123");

        assertEquals("Hanumant Shinde", response.getBody());
        verify(employeeService, times(1)).deleteEmployeeById("123");
    }
}
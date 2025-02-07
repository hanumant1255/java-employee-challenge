package com.reliaquest.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.dozer.DozerBeanMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.reliaquest.api.dto.EmployeeDTO;
import com.reliaquest.api.dto.EmployeeRequest;
import com.reliaquest.api.errorhandlers.APIException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeApiResponse;
import com.reliaquest.api.model.EmployeeListApiResponse;
import com.reliaquest.api.repository.MockEmployeeRestClient;
import com.reliaquest.api.service.EmployeeServiceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private MockEmployeeRestClient mockEmployeeRestClient;

    @Mock
    private DozerBeanMapper dozerBeanMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private EmployeeDTO employeeDTO;
    private EmployeeRequest employeeRequest;
    private EmployeeApiResponse employeeApiResponse;
    private EmployeeListApiResponse employeeListApiResponse;
    private final UUID employeeId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        employeeDTO =
                new EmployeeDTO(employeeId, "Hanumant Shinde", 50000, 30, "Engineer", "hanumantshinde@reliaquest.com");
        Employee employee =
                new Employee(employeeId, "Hanumant Shinde", 50000, 30, "Engineer", "hanumantshinde@reliaquest.com");
        employeeRequest = new EmployeeRequest("Hanumant Shinde", 50000, 30, "Engineer");
        employeeApiResponse = new EmployeeApiResponse(employee, "Success");
        employeeListApiResponse = new EmployeeListApiResponse(Collections.singletonList(employee), "Success");
    }

    @Test
    void getAllEmployees_shouldReturnEmployeeList() {
        when(mockEmployeeRestClient.getAllEmployees()).thenReturn(employeeListApiResponse);
        when(dozerBeanMapper.map(any(), eq(EmployeeDTO.class))).thenReturn(employeeDTO);

        List<EmployeeDTO> employees = employeeService.getAllEmployees();

        assertFalse(employees.isEmpty());
        assertEquals(1, employees.size());
        assertEquals("Hanumant Shinde", employees.get(0).getEmployeeName());
    }

    @Test
    void getEmployeeById_shouldReturnEmployee() {
        when(mockEmployeeRestClient.getEmployeeById(employeeId.toString())).thenReturn(employeeApiResponse);
        when(dozerBeanMapper.map(any(), eq(EmployeeDTO.class))).thenReturn(employeeDTO);

        EmployeeDTO result = employeeService.getEmployeeById(employeeId.toString());

        assertNotNull(result);
        assertEquals("Hanumant Shinde", result.getEmployeeName());
    }

    @Test
    void getEmployeeById_shouldThrowAPIExceptionWhenNotFound() {
        when(mockEmployeeRestClient.getEmployeeById(employeeId.toString())).thenReturn(null);

        APIException exception =
                assertThrows(APIException.class, () -> employeeService.getEmployeeById(employeeId.toString()));
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatusCode());
    }

    @Test
    void getHighestSalaryOfEmployees_shouldReturnMaxSalary() {
        when(mockEmployeeRestClient.getAllEmployees()).thenReturn(employeeListApiResponse);
        when(dozerBeanMapper.map(any(), eq(EmployeeDTO.class))).thenReturn(employeeDTO);

        int highestSalary = employeeService.getHighestSalaryOfEmployees();

        assertEquals(50000, highestSalary);
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_shouldReturnTopNames() {
        List<Employee> employees = Arrays.asList(
                new Employee(UUID.randomUUID(), "Alice", 70000, 30, "Engineer", "alice@reliaquest.com"),
                new Employee(UUID.randomUUID(), "Bob", 60000, 25, "Astronomer", "bob@reliaquest.com"),
                new Employee(UUID.randomUUID(), "Charlie", 50000, 35, "Mathematician", "charlie@reliaquest.com"));

        when(mockEmployeeRestClient.getAllEmployees())
                .thenReturn(new EmployeeListApiResponse(employees, "Successfully processed request"));
        when(dozerBeanMapper.map(any(Employee.class), eq(EmployeeDTO.class))).thenAnswer(invocation -> {
            Employee emp = invocation.getArgument(0);
            return new EmployeeDTO(
                    emp.getId(),
                    emp.getEmployeeName(),
                    emp.getEmployeeSalary(),
                    emp.getEmployeeAge(),
                    emp.getEmployeeTitle(),
                    emp.getEmployeeEmail());
        });
        List<String> topEmployees = employeeService.getTopTenHighestEarningEmployeeNames();

        assertEquals(3, topEmployees.size());
        assertEquals("Alice", topEmployees.get(0));
        assertEquals("Bob", topEmployees.get(1));
        assertEquals("Charlie", topEmployees.get(2));
    }

    @Test
    void createEmployee_shouldReturnCreatedEmployee() {
        when(mockEmployeeRestClient.createEmployee(any())).thenReturn(employeeApiResponse);
        when(dozerBeanMapper.map(any(), eq(EmployeeDTO.class))).thenReturn(employeeDTO);

        EmployeeDTO result = employeeService.createEmployee(employeeRequest);

        assertNotNull(result);
        assertEquals("Hanumant Shinde", result.getEmployeeName());
    }

    @Test
    void deleteEmployeeById_shouldReturnDeletedEmployeeName() {
        when(mockEmployeeRestClient.getEmployeeById(employeeId.toString())).thenReturn(employeeApiResponse);
        when(mockEmployeeRestClient.deleteEmployeeByName("Hanumant Shinde")).thenReturn(true);
        when(dozerBeanMapper.map(any(), eq(EmployeeDTO.class))).thenReturn(employeeDTO);

        String deletedEmployeeName = employeeService.deleteEmployeeById(employeeId.toString());

        assertEquals("Hanumant Shinde", deletedEmployeeName);
    }

    @Test
    void deleteEmployeeById_shouldThrowExceptionWhenDeletionFails() {
        when(mockEmployeeRestClient.getEmployeeById(employeeId.toString())).thenReturn(employeeApiResponse);
        when(mockEmployeeRestClient.deleteEmployeeByName("Hanumant Shinde")).thenReturn(false);
        when(dozerBeanMapper.map(any(), eq(EmployeeDTO.class))).thenReturn(employeeDTO);

        APIException exception =
                assertThrows(APIException.class, () -> employeeService.deleteEmployeeById(employeeId.toString()));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatusCode());
    }

    @Test
    void validateUUID_shouldThrowExceptionForInvalidUUID() {
        APIException exception =
                assertThrows(APIException.class, () -> employeeService.getEmployeeById("invalid-uuid"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatusCode());
    }

    @Test
    void getEmployeesByNameSearch_shouldReturnMatchingEmployees() {
        List<Employee> employees = Arrays.asList(
                new Employee(UUID.randomUUID(), "Alice", 70000, 30, "Engineer", "alice@reliaquest.com"),
                new Employee(UUID.randomUUID(), "Bob", 60000, 25, "Astronomer", "bob@reliaquest.com"),
                new Employee(UUID.randomUUID(), "Charlie", 50000, 35, "Mathematician", "charlie@reliaquest.com"));

        when(mockEmployeeRestClient.getAllEmployees()).thenReturn(new EmployeeListApiResponse(employees, "Success"));
        when(dozerBeanMapper.map(any(Employee.class), eq(EmployeeDTO.class))).thenAnswer(invocation -> {
            Employee emp = invocation.getArgument(0);
            return new EmployeeDTO(
                    emp.getId(),
                    emp.getEmployeeName(),
                    emp.getEmployeeSalary(),
                    emp.getEmployeeAge(),
                    emp.getEmployeeTitle(),
                    emp.getEmployeeEmail());
        });

        List<EmployeeDTO> result = employeeService.getEmployeesByNameSearch("Alice");

        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getEmployeeName());
    }

    @Test
    void getEmployeesByNameSearch_shouldReturnEmptyListWhenNoMatch() {
        List<Employee> employees = Arrays.asList(
                new Employee(UUID.randomUUID(), "Alice", 70000, 30, "Engineer", "alice@reliaquest.com"),
                new Employee(UUID.randomUUID(), "Bob", 60000, 25, "Astronomer", "bob@reliaquest.com"));

        when(mockEmployeeRestClient.getAllEmployees()).thenReturn(new EmployeeListApiResponse(employees, "Success"));
        when(dozerBeanMapper.map(any(Employee.class), eq(EmployeeDTO.class))).thenAnswer(invocation -> {
            Employee emp = invocation.getArgument(0);
            return new EmployeeDTO(
                    emp.getId(),
                    emp.getEmployeeName(),
                    emp.getEmployeeSalary(),
                    emp.getEmployeeAge(),
                    emp.getEmployeeTitle(),
                    emp.getEmployeeEmail());
        });

        List<EmployeeDTO> result = employeeService.getEmployeesByNameSearch("Charlie");

        assertEquals(0, result.size());
    }

    @Test
    void getHighestSalaryOfEmployees_shouldReturnZeroWhenNoEmployees() {
        when(mockEmployeeRestClient.getAllEmployees())
                .thenReturn(new EmployeeListApiResponse(Collections.emptyList(), "Success"));

        int highestSalary = employeeService.getHighestSalaryOfEmployees();

        assertEquals(0, highestSalary);
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_shouldReturnEmptyListWhenNoEmployees() {
        when(mockEmployeeRestClient.getAllEmployees())
                .thenReturn(new EmployeeListApiResponse(Collections.emptyList(), "Success"));

        List<String> topEmployees = employeeService.getTopTenHighestEarningEmployeeNames();

        assertEquals(0, topEmployees.size());
    }
}

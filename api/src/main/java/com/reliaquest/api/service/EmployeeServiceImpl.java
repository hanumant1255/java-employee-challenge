package com.reliaquest.api.service;

import static com.reliaquest.api.util.Constants.MOCK_SERVICE_API_CIRCUIT_BREAKER;
import static com.reliaquest.api.util.Constants.MOCK_SERVICE_API_RETRY;

import com.reliaquest.api.dto.EmployeeDTO;
import com.reliaquest.api.dto.EmployeeRequest;
import com.reliaquest.api.errorhandlers.APIException;
import com.reliaquest.api.model.EmployeeApiResponse;
import com.reliaquest.api.model.EmployeeListApiResponse;
import com.reliaquest.api.repository.MockEmployeeRestClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dozer.DozerBeanMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
@Slf4j
@AllArgsConstructor
public class EmployeeServiceImpl implements EmployeeService<EmployeeDTO, EmployeeRequest> {

    private final MockEmployeeRestClient mockEmployeeRestClient;
    private final DozerBeanMapper dozerBeanMapper;

    @Retry(name = MOCK_SERVICE_API_RETRY)
    @CircuitBreaker(name = MOCK_SERVICE_API_CIRCUIT_BREAKER)
    @Override
    public List<EmployeeDTO> getAllEmployees() {
        log.debug("Fetching all employees from the API");
        EmployeeListApiResponse employeeListApiResponse = mockEmployeeRestClient.getAllEmployees();
        List<EmployeeDTO> employees = employeeListApiResponse.getData().stream()
                .map(employee -> dozerBeanMapper.map(employee, EmployeeDTO.class))
                .collect(Collectors.toList());
        log.debug("Successfully retrieved {} employees from API", employees.size());
        return employees;
    }

    @Retry(name = MOCK_SERVICE_API_RETRY)
    @CircuitBreaker(name = MOCK_SERVICE_API_CIRCUIT_BREAKER)
    @Override
    public List<EmployeeDTO> getEmployeesByNameSearch(String searchString) {
        log.info("Searching employees by name: '{}'", searchString);
        List<EmployeeDTO> employees = getAllEmployees().stream()
                .filter(employee -> employee.getEmployeeName().toLowerCase().contains(searchString.toLowerCase()))
                .collect(Collectors.toList());
        log.info("Found {} employees matching '{}'", employees.size(), searchString);
        return employees;
    }

    @Retry(name = MOCK_SERVICE_API_RETRY)
    @CircuitBreaker(name = MOCK_SERVICE_API_CIRCUIT_BREAKER)
    @Override
    public EmployeeDTO getEmployeeById(String id) {
        EmployeeDTO employee;
        try {
            log.info("Fetching employee with ID: {}", id);
            validateUUID(id);
            EmployeeApiResponse employeeApiResponse = mockEmployeeRestClient.getEmployeeById(id);
            if (employeeApiResponse == null || employeeApiResponse.getData() == null) {
                log.error("Employee with ID {} not found", id);
                throw new APIException("object.not.found", new Object[] {id}, HttpStatus.NOT_FOUND);
            }
            employee = dozerBeanMapper.map(employeeApiResponse.getData(), EmployeeDTO.class);
            log.info("Successfully retrieved employee with ID: {}", id);
        } catch (HttpClientErrorException.NotFound exception) {
            throw new APIException("object.not.found", new Object[] {id}, HttpStatus.NOT_FOUND);
        }
        return employee;
    }

    @Retry(name = MOCK_SERVICE_API_RETRY)
    @CircuitBreaker(name = MOCK_SERVICE_API_CIRCUIT_BREAKER)
    @Override
    public Integer getHighestSalaryOfEmployees() {
        log.info("Fetching highest salary of employees");
        return getAllEmployees().stream()
                .mapToInt(EmployeeDTO::getEmployeeSalary)
                .max()
                .orElse(0);
    }

    @Retry(name = MOCK_SERVICE_API_RETRY)
    @CircuitBreaker(name = MOCK_SERVICE_API_CIRCUIT_BREAKER)
    @Override
    public List<String> getTopTenHighestEarningEmployeeNames() {
        log.info("Fetching top 10 highest earning employees");
        List<String> topEmployees = getAllEmployees().stream()
                .sorted((e1, e2) -> e2.getEmployeeSalary().compareTo(e1.getEmployeeSalary()))
                .limit(10)
                .map(EmployeeDTO::getEmployeeName)
                .collect(Collectors.toList());
        log.info("Successfully retrieved top 10 highest earning employees");
        return topEmployees;
    }

    @Retry(name = MOCK_SERVICE_API_RETRY)
    @CircuitBreaker(name = MOCK_SERVICE_API_CIRCUIT_BREAKER)
    @Override
    public EmployeeDTO createEmployee(EmployeeRequest employeeRequest) {
        log.info("Creating new employee with request data: {}", employeeRequest);
        EmployeeApiResponse employeeApiResponse = mockEmployeeRestClient.createEmployee(employeeRequest);
        EmployeeDTO createdEmployee = dozerBeanMapper.map(employeeApiResponse.getData(), EmployeeDTO.class);
        log.info("Successfully created employee with ID: {}", createdEmployee.getId());
        return createdEmployee;
    }

    @Retry(name = MOCK_SERVICE_API_RETRY)
    @CircuitBreaker(name = MOCK_SERVICE_API_CIRCUIT_BREAKER)
    @Override
    public String deleteEmployeeById(String id) {
        log.info("Deleting employee with ID: {}", id);
        validateUUID(id);
        var employee = getEmployeeById(id);
        if (Boolean.FALSE.equals(mockEmployeeRestClient.deleteEmployeeByName(employee.getEmployeeName()))) {
            throw new APIException("failed.to.delete.record", new Object[] {}, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        log.info("Successfully deleted employee with ID: {}", id);
        return employee.getEmployeeName();
    }

    private void validateUUID(String id) {
        try {
            UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format for ID: {}", id);
            throw new APIException("invalid.uuid.format", new Object[] {id}, HttpStatus.BAD_REQUEST);
        }
    }
}

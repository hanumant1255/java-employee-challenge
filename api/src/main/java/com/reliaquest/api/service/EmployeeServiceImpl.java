package com.reliaquest.api.service;

import static com.reliaquest.api.util.Constants.FAILED_TO_DELETE_RECORD;
import static com.reliaquest.api.util.Constants.INVALID_UUID_FORMAT;
import static com.reliaquest.api.util.Constants.MOCK_SERVICE_API_CIRCUIT_BREAKER;
import static com.reliaquest.api.util.Constants.MOCK_SERVICE_API_RETRY;
import static com.reliaquest.api.util.Constants.OBJECT_NOT_FOUND;

import com.reliaquest.api.dto.EmployeeDTO;
import com.reliaquest.api.dto.EmployeeDeleteRequest;
import com.reliaquest.api.dto.EmployeeRequest;
import com.reliaquest.api.errorhandlers.APIException;
import com.reliaquest.api.model.EmployeeApiResponse;
import com.reliaquest.api.model.EmployeeDeleteApiResponse;
import com.reliaquest.api.model.EmployeeListApiResponse;
import com.reliaquest.api.repository.MockEmployeeRestClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.Comparator;
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
@Retry(name = MOCK_SERVICE_API_RETRY)
@CircuitBreaker(name = MOCK_SERVICE_API_CIRCUIT_BREAKER)
public class EmployeeServiceImpl implements EmployeeService<EmployeeDTO, EmployeeRequest> {

    private final MockEmployeeRestClient mockEmployeeRestClient;
    private final DozerBeanMapper dozerBeanMapper;

    @Override
    public List<EmployeeDTO> getAllEmployees() {
        EmployeeListApiResponse response = mockEmployeeRestClient.getAllEmployees();
        List<EmployeeDTO> employees = mapEmployeeList(response);
        log.debug("Retrieved {} employees", employees.size());
        return employees;
    }

    @Override
    public List<EmployeeDTO> getEmployeesByNameSearch(String searchString) {
        List<EmployeeDTO> employees = getAllEmployees().stream()
                .filter(employee -> employee.getEmployeeName().toLowerCase().contains(searchString.toLowerCase()))
                .collect(Collectors.toList());
        log.debug("Employees matching '{}': {}", searchString, employees.size());
        return employees;
    }

    @Override
    public EmployeeDTO getEmployeeById(String id) {
        validateUUID(id);
        try {
            EmployeeApiResponse response = mockEmployeeRestClient.getEmployeeById(id);
            return mapEmployee(response, id);
        } catch (HttpClientErrorException.NotFound ex) {
            log.warn("Employee with ID {} not found", id);
            throw new APIException(OBJECT_NOT_FOUND, new Object[] {id}, HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public Integer getHighestSalaryOfEmployees() {
        return getAllEmployees().stream()
                .mapToInt(EmployeeDTO::getEmployeeSalary)
                .max()
                .orElse(0);
    }

    @Override
    public List<String> getTopTenHighestEarningEmployeeNames() {
        List<String> topEmployees = getAllEmployees().stream()
                .sorted(Comparator.comparingInt(EmployeeDTO::getEmployeeSalary).reversed())
                .limit(10)
                .map(EmployeeDTO::getEmployeeName)
                .collect(Collectors.toList());
        log.debug("Top 10 highest earning employees retrieved");
        return topEmployees;
    }

    @Override
    public EmployeeDTO createEmployee(EmployeeRequest employeeRequest) {
        EmployeeApiResponse response = mockEmployeeRestClient.createEmployee(employeeRequest);
        EmployeeDTO createdEmployee = dozerBeanMapper.map(response.getData(), EmployeeDTO.class);
        log.info("Employee created with ID: {}", createdEmployee.getId());
        return createdEmployee;
    }

    @Override
    public String deleteEmployeeById(String id) {
        validateUUID(id);
        EmployeeDTO employee = getEmployeeById(id);
        boolean isDeleted = deleteEmployeeByName(employee.getEmployeeName());
        if (!isDeleted) {
            log.error("Failed to delete employee with ID: {}", id);
            throw new APIException(FAILED_TO_DELETE_RECORD, new Object[] {}, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        log.info("Employee deleted with ID: {}", id);
        return employee.getEmployeeName();
    }

    private boolean deleteEmployeeByName(String employeeName) {
        EmployeeDeleteRequest request = new EmployeeDeleteRequest(employeeName);
        EmployeeDeleteApiResponse response = mockEmployeeRestClient.deleteEmployeeByName(request);
        return Boolean.TRUE.equals(response.getData());
    }

    private void validateUUID(String id) {
        try {
            UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format: {}", id);
            throw new APIException(INVALID_UUID_FORMAT, new Object[] {id}, HttpStatus.BAD_REQUEST);
        }
    }

    private List<EmployeeDTO> mapEmployeeList(EmployeeListApiResponse response) {
        return response.getData().stream()
                .map(employee -> dozerBeanMapper.map(employee, EmployeeDTO.class))
                .collect(Collectors.toList());
    }

    private EmployeeDTO mapEmployee(EmployeeApiResponse response, String id) {
        if (response == null || response.getData() == null) {
            log.warn("Employee with given ID {} not found", id);
            throw new APIException(OBJECT_NOT_FOUND, new Object[] {id}, HttpStatus.NOT_FOUND);
        }
        return dozerBeanMapper.map(response.getData(), EmployeeDTO.class);
    }
}

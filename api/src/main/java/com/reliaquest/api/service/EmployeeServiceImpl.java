package com.reliaquest.api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.dozer.DozerBeanMapper;
import org.springframework.stereotype.Service;

import com.reliaquest.api.dto.EmployeeDTO;
import com.reliaquest.api.dto.EmployeeRequest;
import com.reliaquest.api.errorhandlers.EmployeeNotFoundException;
import com.reliaquest.api.errorhandlers.EmployeeServiceException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeApiResponse;
import com.reliaquest.api.model.EmployeeListApiResponse;
import com.reliaquest.api.repository.MockEmployeeRestClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Note  - Not adding transaction management support since there is no database involved.
@Service
@Slf4j
@AllArgsConstructor
public class EmployeeServiceImpl implements EmployeeService<EmployeeDTO, EmployeeRequest> {

    private final MockEmployeeRestClient mockEmployeeRestClient;
    private final DozerBeanMapper dozerBeanMapper;

    @Override
    public List<EmployeeDTO> getAllEmployees() {
        log.debug("Fetching all employees from the API");
        EmployeeListApiResponse employeeListApiResponse = mockEmployeeRestClient.getAllEmployees();

        if (employeeListApiResponse == null || employeeListApiResponse.getData() == null) {
            log.error("Failed to fetch employee data from API");
            throw new EmployeeServiceException("Failed to fetch employee data.");
        }

        List<EmployeeDTO> employees = employeeListApiResponse.getData().stream()
                .map(employee -> dozerBeanMapper.map(employee, EmployeeDTO.class))
                .collect(Collectors.toList());

        if (employees.isEmpty()) {
            log.warn("No employees found");
            throw new EmployeeNotFoundException("No employees found.");
        }

        log.debug("Successfully retrieved {} employees from API", employees.size());
        return employees;
    }

    @Override
    public List<EmployeeDTO> getEmployeesByNameSearch(String searchString) {
        log.debug("Searching employees by name: '{}'", searchString);
        List<EmployeeDTO> employees = getAllEmployees().stream()
                .filter(employee -> employee.getEmployeeName().toLowerCase().contains(searchString.toLowerCase()))
                .collect(Collectors.toList());

        if (employees.isEmpty()) {
            log.warn("No employees found matching '{}'", searchString);
            throw new EmployeeNotFoundException("No employees found matching: " + searchString);
        }

        log.debug("Found {} employees matching '{}'", employees.size(), searchString);
        return employees;
    }

    @Override
    public EmployeeDTO getEmployeeById(String id) {
        log.debug("Fetching employee with ID: {}", id);
        EmployeeApiResponse employeeApiResponse = mockEmployeeRestClient.getEmployeeById(id);

        if (employeeApiResponse == null || employeeApiResponse.getData() == null) {
            log.error("Employee with ID {} not found", id);
            throw new EmployeeNotFoundException("Employee not found with ID: " + id);
        }

        EmployeeDTO employee = dozerBeanMapper.map(employeeApiResponse.getData(), EmployeeDTO.class);
        log.debug("Successfully retrieved employee with ID: {}", id);
        return employee;
    }

    @Override
    public Integer getHighestSalaryOfEmployees() {
        log.debug("Fetching highest salary of employees");
        return getAllEmployees().stream()
                .mapToInt(EmployeeDTO::getEmployeeSalary)
                .max()
                .orElseThrow(() -> {
                    log.warn("No employees found to calculate highest salary");
                    return new EmployeeNotFoundException("No employees available to calculate the highest salary.");
                });
    }

    @Override
    public List<String> getTopTenHighestEarningEmployeeNames() {
        log.debug("Fetching top 10 highest earning employees");
        List<String> topEmployees = getAllEmployees().stream()
                .sorted((e1, e2) -> e2.getEmployeeSalary().compareTo(e1.getEmployeeSalary()))
                .limit(10)
                .map(EmployeeDTO::getEmployeeName)
                .collect(Collectors.toList());

        if (topEmployees.isEmpty()) {
            log.warn("No employees found to list top 10 earners");
            throw new EmployeeNotFoundException("No employees available to determine top 10 highest earners.");
        }

        log.debug("Successfully retrieved top 10 highest earning employees");
        return topEmployees;
    }

    @Override
    public EmployeeDTO createEmployee(EmployeeRequest employeeRequest) {
        log.debug("Creating new employee with request data: {}", employeeRequest);
        try {
            Employee employee = dozerBeanMapper.map(employeeRequest, Employee.class);
            employee.setId(null);

            EmployeeApiResponse employeeApiResponse = mockEmployeeRestClient.createEmployee(employee);

            if (employeeApiResponse == null || employeeApiResponse.getData() == null) {
                log.error("Failed to create employee with data: {}", employeeRequest);
                throw new EmployeeServiceException("Employee creation failed.");
            }

            EmployeeDTO createdEmployee = dozerBeanMapper.map(employeeApiResponse.getData(), EmployeeDTO.class);
            log.debug("Successfully created employee with ID: {}", createdEmployee.getId());
            return createdEmployee;
        } catch (Exception e) {
            log.error("Error occurred while creating employee: {}", e.getMessage(), e);
            throw new EmployeeServiceException("Error occurred while creating employee: " + e.getMessage());
        }
    }

    @Override
    public String deleteEmployeeById(String id) {
        log.debug("Deleting employee with ID: {}", id);
        try {
            var employee = getEmployeeById(id);
            if (Boolean.FALSE.equals(mockEmployeeRestClient.deleteEmployeeByName(employee.getEmployeeName()))) {
                log.error("Failed to delete employee with ID: {}", id);
                throw new EmployeeNotFoundException("Employee deletion failed. No employee found with ID: " + id);
            }
            log.debug("Successfully deleted employee with ID: {}", id);
            return employee.getEmployeeName();
        } catch (Exception e) {
            log.error("Error occurred while deleting employee with ID {}: {}", id, e.getMessage(), e);
            throw new EmployeeServiceException("Error occurred while deleting employee: " + e.getMessage());
        }
    }
}
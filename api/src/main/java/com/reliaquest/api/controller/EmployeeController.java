package com.reliaquest.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reliaquest.api.dto.EmployeeDTO;
import com.reliaquest.api.dto.EmployeeRequest;
import com.reliaquest.api.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Employee Management", description = "Operations related to employee management")
@RestController
@RequestMapping("/api/v2/employee")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController implements IEmployeeController<EmployeeDTO, EmployeeRequest> {

    private final EmployeeService<EmployeeDTO, EmployeeRequest> employeeService;

    @Operation(summary = "Get all employees", description = "This API fetches all the employees present in the system.")
    @Override
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        log.debug("Request to fetch all employees received");
        var employees = employeeService.getAllEmployees();
        log.debug("Successfully retrieved {} employees", employees.size());
        return ResponseEntity.ok(employees);
    }

    @Operation(summary = "Search employees by name", description = "Search employees by name fragment.")
    @Override
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByNameSearch(
            @Parameter(description = "Search string for employee names") String searchString) {
        log.debug("Request to search employees by name with search string: '{}'", searchString);
        var employees = employeeService.getEmployeesByNameSearch(searchString);
        log.debug("Found {} employees matching search string: '{}'", employees.size(), searchString);
        return ResponseEntity.ok(employees);
    }

    @Operation(summary = "Get employee by ID", description = "Fetch employee details by unique ID.")
    @Override
    public ResponseEntity<EmployeeDTO> getEmployeeById(@Parameter(description = "Employee ID") String id) {
        log.debug("Request to fetch employee with ID: {}", id);
        var employee = employeeService.getEmployeeById(id);
        log.debug("Successfully retrieved employee with ID: {}", id);
        return ResponseEntity.ok(employee);
    }

    @Operation(summary = "Get highest salary of employees", description = "Get the highest salary among all employees.")
    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        log.debug("Request to fetch highest salary of employees");
        var highestSalary = employeeService.getHighestSalaryOfEmployees();
        log.debug("Successfully retrieved highest salary: {}", highestSalary);
        return ResponseEntity.ok(highestSalary);
    }

    @Operation(summary = "Get top 10 highest earning employees", description = "Fetch top 10 highest earning employees.")
    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        log.debug("Request to fetch top 10 highest earning employees");
        var topEarningEmployeeNames = employeeService.getTopTenHighestEarningEmployeeNames();
        log.debug("Successfully retrieved top 10 highest earning employees");
        return ResponseEntity.ok(topEarningEmployeeNames);
    }

    @Operation(summary = "Create a new employee", description = "Create a new employee with the provided details.")
    @Override
    public ResponseEntity<EmployeeDTO> createEmployee(@Validated EmployeeRequest employeeInput) {
        log.debug("Request to create a new employee with data: {}", employeeInput);
        var employee = employeeService.createEmployee(employeeInput);
        log.debug("Successfully created employee with ID: {}", employee.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }

    @Operation(summary = "Delete employee by ID", description = "Delete an employee by their unique ID.")
    @Override
    public ResponseEntity<String> deleteEmployeeById(@Parameter(description = "Employee ID") String id) {
        log.debug("Request to delete employee with ID: {}", id);
        var employeeName = employeeService.deleteEmployeeById(id);
        log.debug("Successfully deleted employee with ID: {}", id);
        return ResponseEntity.ok(employeeName);
    }
}
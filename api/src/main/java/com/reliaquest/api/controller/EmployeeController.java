package com.reliaquest.api.controller;

import static com.reliaquest.api.util.ApiDescriptions.CREATE_EMPLOYEE_DESC;
import static com.reliaquest.api.util.ApiDescriptions.CREATE_EMPLOYEE_SUMMARY;
import static com.reliaquest.api.util.ApiDescriptions.DELETE_EMPLOYEE_DESC;
import static com.reliaquest.api.util.ApiDescriptions.DELETE_EMPLOYEE_SUMMARY;
import static com.reliaquest.api.util.ApiDescriptions.GET_ALL_EMPLOYEES_DESC;
import static com.reliaquest.api.util.ApiDescriptions.GET_ALL_EMPLOYEES_SUMMARY;
import static com.reliaquest.api.util.ApiDescriptions.GET_EMPLOYEE_BY_ID_DESC;
import static com.reliaquest.api.util.ApiDescriptions.GET_EMPLOYEE_BY_ID_SUMMARY;
import static com.reliaquest.api.util.ApiDescriptions.GET_HIGHEST_SALARY_DESC;
import static com.reliaquest.api.util.ApiDescriptions.GET_HIGHEST_SALARY_SUMMARY;
import static com.reliaquest.api.util.ApiDescriptions.GET_TOP_EARNERS_DESC;
import static com.reliaquest.api.util.ApiDescriptions.GET_TOP_EARNERS_SUMMARY;
import static com.reliaquest.api.util.ApiDescriptions.SEARCH_EMPLOYEES_DESC;
import static com.reliaquest.api.util.ApiDescriptions.SEARCH_EMPLOYEES_SUMMARY;

import com.reliaquest.api.dto.EmployeeDTO;
import com.reliaquest.api.dto.EmployeeRequest;
import com.reliaquest.api.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Employee Management", description = "Operations related to employee management")
@RestController
@RequestMapping("/api/v2/employee")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController implements IEmployeeController<EmployeeDTO, EmployeeRequest> {

    private final EmployeeService<EmployeeDTO, EmployeeRequest> employeeService;

    @Operation(summary = GET_ALL_EMPLOYEES_SUMMARY, description = GET_ALL_EMPLOYEES_DESC)
    @Override
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        log.debug("Fetching all employees...");
        List<EmployeeDTO> employees = employeeService.getAllEmployees();
        log.info("Retrieved {} employees successfully.", employees.size());
        return ResponseEntity.ok(employees);
    }

    @Operation(summary = SEARCH_EMPLOYEES_SUMMARY, description = SEARCH_EMPLOYEES_DESC)
    @Override
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByNameSearch(
            @PathVariable @Parameter(description = "Search string for employee names") String searchString) {
        log.debug("Searching employees by name: '{}'", searchString);
        List<EmployeeDTO> employees = employeeService.getEmployeesByNameSearch(searchString);
        log.info("Found {} employees matching search string '{}'", employees.size(), searchString);
        return ResponseEntity.ok(employees);
    }

    @Operation(summary = GET_EMPLOYEE_BY_ID_SUMMARY, description = GET_EMPLOYEE_BY_ID_DESC)
    @Override
    public ResponseEntity<EmployeeDTO> getEmployeeById(
            @PathVariable @Parameter(description = "Employee ID") String id) {
        log.debug("Fetching employee with ID: {}", id);
        EmployeeDTO employee = employeeService.getEmployeeById(id);
        log.info("Retrieved employee with ID: {}", id);
        return ResponseEntity.ok(employee);
    }

    @Operation(summary = GET_HIGHEST_SALARY_SUMMARY, description = GET_HIGHEST_SALARY_DESC)
    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        log.debug("Fetching highest employee salary...");
        int highestSalary = employeeService.getHighestSalaryOfEmployees();
        log.info("Highest salary retrieved: {}", highestSalary);
        return ResponseEntity.ok(highestSalary);
    }

    @Operation(summary = GET_TOP_EARNERS_SUMMARY, description = GET_TOP_EARNERS_DESC)
    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        log.debug("Fetching top 10 highest earning employees...");
        List<String> topEarners = employeeService.getTopTenHighestEarningEmployeeNames();
        log.info("Retrieved top 10 highest earning employees.");
        return ResponseEntity.ok(topEarners);
    }

    @Operation(summary = CREATE_EMPLOYEE_SUMMARY, description = CREATE_EMPLOYEE_DESC)
    @Override
    public ResponseEntity<EmployeeDTO> createEmployee(@RequestBody @Validated EmployeeRequest employeeInput) {
        log.debug("Creating a new employee: {}", employeeInput);
        EmployeeDTO employee = employeeService.createEmployee(employeeInput);
        log.info("Employee created successfully with ID: {}", employee.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(employee);
    }

    @Operation(summary = DELETE_EMPLOYEE_SUMMARY, description = DELETE_EMPLOYEE_DESC)
    @Override
    public ResponseEntity<String> deleteEmployeeById(@PathVariable @Parameter(description = "Employee ID") String id) {
        log.debug("Deleting employee with ID: {}", id);
        String employeeName = employeeService.deleteEmployeeById(id);
        log.info("Successfully deleted employee with ID: {}", id);
        return ResponseEntity.ok(employeeName);
    }
}

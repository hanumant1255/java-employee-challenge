package com.reliaquest.api.util;

public class ApiDescriptions {

    private ApiDescriptions() {}

    public static final String GET_ALL_EMPLOYEES_SUMMARY = "Get all employees";
    public static final String GET_ALL_EMPLOYEES_DESC = "Fetches all employees present in the system.";

    public static final String SEARCH_EMPLOYEES_SUMMARY = "Search employees by name";
    public static final String SEARCH_EMPLOYEES_DESC = "Search employees by name fragment.";

    public static final String GET_EMPLOYEE_BY_ID_SUMMARY = "Get employee by ID";
    public static final String GET_EMPLOYEE_BY_ID_DESC = "Fetch employee details by unique ID.";

    public static final String GET_HIGHEST_SALARY_SUMMARY = "Get highest salary of employees";
    public static final String GET_HIGHEST_SALARY_DESC = "Fetch highest salary among all employees.";

    public static final String GET_TOP_EARNERS_SUMMARY = "Get top 10 highest earning employees";
    public static final String GET_TOP_EARNERS_DESC = "Fetch top 10 highest earning employees.";

    public static final String CREATE_EMPLOYEE_SUMMARY = "Create a new employee";
    public static final String CREATE_EMPLOYEE_DESC = "Create an employee with provided details.";

    public static final String DELETE_EMPLOYEE_SUMMARY = "Delete employee by ID";
    public static final String DELETE_EMPLOYEE_DESC = "Delete an employee by their unique ID.";
}

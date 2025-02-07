package com.reliaquest.api.service;

import java.util.List;

public interface EmployeeService<Entity, Input> {

    List<Entity> getAllEmployees();

    List<Entity> getEmployeesByNameSearch(String searchString);

    Entity getEmployeeById(String id);

    Integer getHighestSalaryOfEmployees();

    List<String> getTopTenHighestEarningEmployeeNames();

    Entity createEmployee(Input input);

    String deleteEmployeeById(String id);
}

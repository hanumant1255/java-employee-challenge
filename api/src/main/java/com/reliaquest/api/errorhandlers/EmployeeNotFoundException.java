package com.reliaquest.api.errorhandlers;

import static com.reliaquest.api.util.Constants.EMPLOYEE_NOT_FOUND;

public class EmployeeNotFoundException extends APIException {

    public EmployeeNotFoundException(String message) {
        super(EMPLOYEE_NOT_FOUND, new Object[] {message});
    }
}

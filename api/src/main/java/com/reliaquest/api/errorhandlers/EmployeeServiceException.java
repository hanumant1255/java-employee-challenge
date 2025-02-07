package com.reliaquest.api.errorhandlers;

import static com.reliaquest.api.util.Constants.EMPLOYEE_SERVICE_ERROR;

public class EmployeeServiceException extends APIException {
    public EmployeeServiceException(String message) {
        super(EMPLOYEE_SERVICE_ERROR, new Object[] {message});
    }
}

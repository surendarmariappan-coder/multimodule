package org.example.employeeservice.employees;

import org.example.common.exceptions.NotFoundException;

public class EmployeeNotFoundException extends NotFoundException {
    public EmployeeNotFoundException(long id) {
        super("Employee not found: id=" + id);
    }
}

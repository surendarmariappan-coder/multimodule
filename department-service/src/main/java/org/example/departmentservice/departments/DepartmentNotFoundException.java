package org.example.departmentservice.departments;

import org.example.common.exceptions.NotFoundException;

public class DepartmentNotFoundException extends NotFoundException {
    public DepartmentNotFoundException(long id) {
        super("Department not found: id=" + id);
    }
}

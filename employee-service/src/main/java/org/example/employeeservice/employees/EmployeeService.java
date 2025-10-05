package org.example.employeeservice.employees;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {
    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    public List<Employee> findAll() {
        return repository.findAll();
    }

    public Employee findByIdOrThrow(long id) {
        return repository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    public Employee create(Employee e) {
        return repository.create(e);
    }

    public Employee update(long id, Employee e) {
        boolean updated = repository.update(id, e);
        if (!updated) throw new EmployeeNotFoundException(id);
        return new Employee(id, e.firstName(), e.lastName(), e.email(), e.departmentId());
    }

    public void delete(long id) {
        boolean deleted = repository.delete(id);
        if (!deleted) throw new EmployeeNotFoundException(id);
    }
}

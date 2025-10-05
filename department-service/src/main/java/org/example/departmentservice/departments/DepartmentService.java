package org.example.departmentservice.departments;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {
    private final DepartmentRepository repository;

    public DepartmentService(DepartmentRepository repository) {
        this.repository = repository;
    }

    public List<Department> findAll() {
        return repository.findAll();
    }

    public Department findByIdOrThrow(long id) {
        return repository.findById(id).orElseThrow(() -> new DepartmentNotFoundException(id));
    }

    public Department create(Department d) {
        return repository.create(d);
    }

    public Department update(long id, Department d) {
        boolean updated = repository.update(id, d);
        if (!updated) throw new DepartmentNotFoundException(id);
        return new Department(id, d.name(), d.description());
    }

    public void delete(long id) {
        boolean deleted = repository.delete(id);
        if (!deleted) throw new DepartmentNotFoundException(id);
    }
}

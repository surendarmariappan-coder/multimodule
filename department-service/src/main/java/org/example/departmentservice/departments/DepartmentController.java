package org.example.departmentservice.departments;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService service;

    public DepartmentController(DepartmentService service) {
        this.service = service;
    }

    @GetMapping
    public List<Department> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Department getById(@PathVariable long id) {
        return service.findByIdOrThrow(id);
    }

    public record CreateUpdateDepartment(@NotBlank String name, String description) {}

    @PostMapping
    public ResponseEntity<Department> create(@Valid @RequestBody CreateUpdateDepartment req) {
        Department created = service.create(new Department(null, req.name(), req.description()));
        return ResponseEntity.created(URI.create("/api/departments/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public Department update(@PathVariable long id, @Valid @RequestBody CreateUpdateDepartment req) {
        return service.update(id, new Department(id, req.name(), req.description()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        service.delete(id);
    }

}

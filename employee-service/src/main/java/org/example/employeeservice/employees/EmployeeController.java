package org.example.employeeservice.employees;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping
    public List<Employee> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Employee getById(@PathVariable long id) {
        return service.findByIdOrThrow(id);
    }

    public record CreateUpdateEmployee(
            @NotBlank @Size(max = 255) String firstName,
            @NotBlank @Size(max = 255) String lastName,
            @NotBlank @Email @Size(max = 320) String email,
            Long departmentId
    ) {}

    @PostMapping
    public ResponseEntity<Employee> create(@Valid @RequestBody CreateUpdateEmployee req) {
        Employee created = service.create(new Employee(null, req.firstName(), req.lastName(), req.email(), req.departmentId()));
        return ResponseEntity.created(URI.create("/api/employees/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public Employee update(@PathVariable long id, @Valid @RequestBody CreateUpdateEmployee req) {
        return service.update(id, new Employee(id, req.firstName(), req.lastName(), req.email(), req.departmentId()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        service.delete(id);
    }

}

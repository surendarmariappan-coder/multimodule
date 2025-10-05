package org.example.employeeservice.employees;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class EmployeeRepository {

    private final JdbcClient jdbc;

    public EmployeeRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<Employee> findAll() {
        return jdbc.sql("SELECT id, first_name, last_name, email, department_id FROM Employees ORDER BY id")
                .query((rs, rowNum) -> new Employee(
                        rs.getLong("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getObject("department_id") == null ? null : rs.getLong("department_id")
                ))
                .list();
    }

    public Optional<Employee> findById(long id) {
        return jdbc.sql("SELECT id, first_name, last_name, email, department_id FROM Employees WHERE id = :id")
                .param("id", id)
                .query((rs, rowNum) -> new Employee(
                        rs.getLong("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getObject("department_id") == null ? null : rs.getLong("department_id")
                ))
                .optional();
    }

    public Employee create(Employee e) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.sql("INSERT INTO Employees(first_name, last_name, email, department_id, created_at, updated_at) VALUES(:firstName, :lastName, :email, :departmentId, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")
                .param("firstName", e.firstName())
                .param("lastName", e.lastName())
                .param("email", e.email())
                .param("departmentId", e.departmentId())
                .update(keyHolder);

        // Extract the ID specifically from the key map
        Number key = (Number) keyHolder.getKeys().get("ID");
        Long id = key != null ? key.longValue() : null;

        return new Employee(id, e.firstName(), e.lastName(), e.email(), e.departmentId());
    }

    public boolean update(long id, Employee e) {
        int rows = jdbc.sql("UPDATE Employees SET first_name = :firstName, last_name = :lastName, email = :email, department_id = :departmentId, updated_at = CURRENT_TIMESTAMP WHERE id = :id")
                .param("firstName", e.firstName())
                .param("lastName", e.lastName())
                .param("email", e.email())
                .param("departmentId", e.departmentId())
                .param("id", id)
                .update();
        return rows > 0;
    }

    public boolean delete(long id) {
        int rows = jdbc.sql("DELETE FROM Employees WHERE id = :id")
                .param("id", id)
                .update();
        return rows > 0;
    }
}
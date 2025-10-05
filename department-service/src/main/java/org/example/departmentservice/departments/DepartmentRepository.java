package org.example.departmentservice.departments;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DepartmentRepository {

    private final JdbcClient jdbc;

    public DepartmentRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<Department> findAll() {
        return jdbc.sql("SELECT id, name, description FROM Departments ORDER BY id")
                .query((rs, rowNum) -> new Department(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("description")
                ))
                .list();
    }

    public Optional<Department> findById(long id) {
        return jdbc.sql("SELECT id, name, description FROM Departments WHERE id = :id")
                .param("id", id)
                .query((rs, rowNum) -> new Department(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("description")
                ))
                .optional();
    }

    public Department create(Department d) {
        // Insert and then retrieve generated id (H2 specific IDENTITY())
        jdbc.sql("INSERT INTO Departments(name, description, created_at, updated_at) VALUES(:name, :description, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")
                .param("name", d.name())
                .param("description", d.description())
                .update();
        Long id = jdbc.sql("SELECT IDENTITY()")
                .query(Long.class)
                .single();
        return new Department(id, d.name(), d.description());
    }

    public boolean update(long id, Department d) {
        int rows = jdbc.sql("UPDATE Departments SET name = :name, description = :description, updated_at = CURRENT_TIMESTAMP WHERE id = :id")
                .param("name", d.name())
                .param("description", d.description())
                .param("id", id)
                .update();
        return rows > 0;
    }

    public boolean delete(long id) {
        int rows = jdbc.sql("DELETE FROM Departments WHERE id = :id")
                .param("id", id)
                .update();
        return rows > 0;
    }
}

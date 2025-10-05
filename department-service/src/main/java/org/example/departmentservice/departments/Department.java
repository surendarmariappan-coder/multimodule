package org.example.departmentservice.departments;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record Department(
        Long id,
        @NotBlank @Size(max = 255) String name,
        @Size(max = 1024) String description
) {}

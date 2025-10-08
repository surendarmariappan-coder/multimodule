package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "org.example.departmentservice.departments",
        "org.example.employeeservice.employees",
        "org.example.common"
})
public class AssemblyApplication {
    public static void main(String[] args) {
        SpringApplication.run(AssemblyApplication.class, args);
    }
}

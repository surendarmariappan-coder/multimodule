package org.example.departmentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"org.example.departmentservice", "org.example.common"})
public class DepartmentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DepartmentServiceApplication.class, args);
    }
}

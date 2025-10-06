# Multi‑Module Conversion Guide and Prompt Template

This document helps you convert multiple Gradle‑based Spring Boot microservices into a single multi‑module Gradle build, extract shared code into a reusable library (common module), and centralize dependency management at the root. It includes a ready‑to‑use LLM prompt template, step‑by‑step instructions, copy‑pasteable Gradle snippets, and troubleshooting tips.

The repository you’re reading already demonstrates the target structure:
- Root project: workspace
- Modules: common (library jar), department-service (Spring Boot app), employee-service (Spring Boot app)
- Centralized dependency management and shared service dependencies at the root

Use this as a template to migrate other projects following the steps below.

---

## Quick Start (for this repository)
- Build everything: ./gradlew clean build
- Run department-service: ./gradlew :department-service:bootRun
- Run employee-service: ./gradlew :employee-service:bootRun

If you want to publish the common library locally for other projects to consume, see Publishing the common JAR below.

---

## LLM Prompt Template (Copy‑Paste)
Paste the following into your LLM and replace the placeholders with details of your services. Keep your codebase accessible to the LLM if possible.

You are an expert Spring Boot microservice developer. Convert my separate Gradle‑based Spring Boot services into a single multi‑module Gradle project so I can:
- Share code and dependencies across services
- Build a reusable JAR (common module) for cross‑cutting concerns (e.g., exceptions/handlers)
- Keep one service as primary and add others as modules to the primary service
- Centralize dependency management at the root build.gradle
- Run each service as a Spring Boot app
- Publish the common JAR locally for other projects to consume

My current services:
- Service A: <name/path, key dependencies, Java version>
- Service B: <name/path, key dependencies, Java version>
- Add Service A as a primary service and add Service B as a module
- (Add more if needed)

Target layout:
- Root project: <root-name>
- Modules: common (java-library), <service-a> (Spring Boot app), <service-b> (Spring Boot app)

Requirements:
1. Centralize dependency management in the root build.gradle:
   - Use plugins with apply false for org.springframework.boot and io.spring.dependency-management and other plugins that are not needed at the root level
   - Configure subprojects with group/version, repositories, Java toolchain, and JUnit Platform
   - Import Spring Boot,Spring Cloud BOMs, and related BOMs via dependencyManagement block 
   - Add shared service dependencies (example: web, jdbc, validation, test, etc) at the root for all application modules
2. Module adjustments:
   - common is a java-library that exposes cross‑cutting code (exceptions, @ControllerAdvice and other common functionalties across services)
   - Application modules depend on project(':common')
   - Include common module to be scanned across all dependent modules
   - Remove duplicated dependencies from service modules that are now provided by the root
   - Keep module‑specific configuration minimal (description, application properties)
3. Build and verify from the root: ./gradlew clean build
4. Provide final instructions to run each service (bootRun) and how to publish the common JAR locally

---

## Migration Checklist (General)
1. Create a new root project directory (or reuse your monorepo root)
   - Add settings.gradle with include 'common', '<service-1>', '<service-2>'
2. Move each existing service into its own subdirectory under the root
   - Example: department-service/, employee-service/
   - Keep each service’s src/main/java and src/main/resources content
3. Create a common module for shared code
   - Example: common/src/main/java/... for exceptions and a @ControllerAdvice
4. Centralize dependency management at the root build.gradle
   - Apply Spring Boot and dependency-management plugins with apply false
   - Add dependencyManagement importing Spring Boot and Spring Cloud BOMs
   - Configure subprojects with repositories, toolchain, and tests
   - Add shared service dependencies via a configure block for your application modules
5. Update each module build.gradle
   - common: use java-library; no Spring Boot plugin; publish a plain jar
   - services: minimal build.gradle; rely on root for dependencies
6. Update package imports in services to use exceptions/handlers from common
7. Build from root: ./gradlew clean build
8. Run services as needed: ./gradlew :service-name:bootRun
9. (Optional) Publish common locally for use by other repos

---

## Root settings.gradle (Example)
rootProject.name = 'workspace'
include 'common', 'department-service', 'employee-service'

---

## Root build.gradle (Template)
plugins {
    id 'base'
    id 'org.springframework.boot' version '3.5.6' apply false
    id 'io.spring.dependency-management' version '1.1.7' apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}

apply plugin: 'io.spring.dependency-management'

dependencyManagement {
    imports {
        mavenBom "org.springframework.boot:spring-boot-dependencies:3.5.6"
        mavenBom "org.springframework.cloud:spring-cloud-dependencies:2024.0.3"
    }
}

subprojects {
    apply plugin: 'io.spring.dependency-management'
    apply plugin: 'java'

    group = 'org.example'
    version = '0.0.1-SNAPSHOT'

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    tasks.named('test') {
        useJUnitPlatform()
    }
}

// Shared dependencies for application modules only
configure([project(":department-service"), project(":employee-service")]) {
    apply plugin: 'org.springframework.boot'

    dependencies {
        implementation project(':common')

        implementation 'org.springframework.boot:spring-boot-starter-web'
        implementation 'org.springframework.boot:spring-boot-starter-jdbc'
        implementation 'org.springframework.boot:spring-boot-starter-validation'

        runtimeOnly 'com.h2database:h2'

        testImplementation 'org.springframework.boot:spring-boot-starter-test'
        testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
    }
}

// Aggregate build
tasks.named("build") {
    group = "build"
    description = "Builds all subprojects"
    dependsOn(subprojects.collect { it.tasks.named("build") })
}

Notes:
- Do not apply the Spring Boot plugin to the common library.
- Let versions be managed by BOMs; avoid hardcoding versions in subprojects.

---

## common/build.gradle (Template)
plugins {
    id 'java-library'
}

description = 'Common library for shared code (exceptions, handlers)'

// Prefer no explicit versions; rely on BOMs from root (dependencyManagement)
dependencies {
    api 'org.springframework.boot:spring-boot-starter-web'
}

// Optional: If you want to disable bootJar here (should not be applied anyway):
// tasks.named('jar') { enabled = true }

---

## Service module build.gradle (Template)
// Configuration is centralized in the root build.gradle for all modules.
description = 'DepartmentService' // or EmployeeService

// No dependencies block needed; root config applies Spring Boot plugin and dependencies.

---

## Example: Shared Exceptions and Global Handler in common
// File: common/src/main/java/org/example/common/exceptions/NotFoundException.java
package org.example.common.exceptions;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}

// File: common/src/main/java/org/example/common/web/ErrorResponse.java
package org.example.common.web;

public record ErrorResponse(String code, String message) {}

// File: common/src/main/java/org/example/common/web/GlobalExceptionHandler.java
package org.example.common.web;

import org.example.common.exceptions.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", ex.getMessage()));
    }
}

Usage from services:
- Throw org.example.common.exceptions.NotFoundException from your service layer.
- Do not add local @ControllerAdvice in each service; the common handler will be picked up if the package is scanned. With Spring Boot, component scanning starts from your main app package; ensure the common package is visible via dependency and classpath (as it is). If necessary, add @ComponentScan basePackages to include org.example.common.

---

## Commands
- Build all: ./gradlew clean build
- Run a service: ./gradlew :department-service:bootRun or ./gradlew :employee-service:bootRun
- Run tests for all: ./gradlew test
- Run tests for one module: ./gradlew :common:test

---

## Publishing the common JAR (Optional)
If you want to publish common to your local Maven repository so other repos can consume it:

In common/build.gradle:
plugins {
    id 'java-library'
    id 'maven-publish'
}

publishing {
    publications {
        mavenJava(MavenPublication) {
            from components.java
            groupId = 'org.example'
            artifactId = 'common'
            version = '0.0.1-SNAPSHOT'
        }
    }
}

Then publish: ./gradlew :common:publishToMavenLocal
Consumers can then use: implementation 'org.example:common:0.0.1-SNAPSHOT'

---

## Troubleshooting
- Classes from common not found at runtime
  - Ensure service modules have implementation project(':common') (in this repo it is wired at the root).
  - Ensure package scanning can see common’s @ControllerAdvice. If your main app is in org.example.departmentservice, Spring scans subpackages. Common is org.example.common, which is a sibling; you may add @SpringBootApplication(scanBasePackages = {"org.example"}).

- Version conflicts or duplicate versions
  - Keep versions out of subprojects; rely on BOMs defined in root dependencyManagement.
  - Do not apply Boot plugin to the common module.

- H2 not starting or schema issues
  - Confirm schema.sql is on the classpath under src/main/resources.
  - Check application.properties for correct spring.datasource.* properties.

- Gradle cache issues after refactor
  - Try ./gradlew clean build --refresh-dependencies

---

## Next Steps
- Use the LLM Prompt Template to convert additional services to modules.
- Move any other cross‑cutting code (logging config, error codes, security) into common.
- Add CI to build modules from root and optionally publish the common JAR.